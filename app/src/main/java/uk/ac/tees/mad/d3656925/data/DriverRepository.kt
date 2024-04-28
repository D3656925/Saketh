package uk.ac.tees.mad.d3656925.data

import android.content.Context
import com.google.firebase.Firebase
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.auth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.GeoPoint
import com.google.firebase.firestore.SetOptions
import com.google.firebase.firestore.toObject
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import uk.ac.tees.mad.d3656925.domain.Resource
import uk.ac.tees.mad.d3656925.domain.TripDetail
import uk.ac.tees.mad.d3656925.utils.location.PreferencesManager
import javax.inject.Inject

interface DriverRepository {
    fun addNewTrip(
        startLocation: String,
        endLocation: String,
        startTime: Long,
        price: Double,
        coordinate: GeoPoint
    ): Flow<Resource<String>>

    fun getTripDetails(tripId: String): Flow<Resource<TripDetail>>
    fun getCurrentTripForDriver(): Flow<Resource<TripDetail>>
    fun markTripAsCompleted(tripId: String): Flow<Resource<String>>
    fun cancelTrip(tripId: String): Flow<Resource<String>>
    fun getAllPastTrips(userId: String): Flow<Resource<List<TripDetail>>>
    fun updateDriverLocation(driverId: String, newLocation: GeoPoint): Flow<Resource<Void>>
}

class DriverRepositoryImplementation @Inject constructor(
    private val firebaseAuth: FirebaseAuth,
    private val firestore: FirebaseFirestore,
    @ApplicationContext private val context: Context
) : DriverRepository {
    val preferenceManager = PreferencesManager(context = context)

    override fun addNewTrip(
        startLocation: String,
        endLocation: String,
        startTime: Long,
        price: Double,
        coordinate: GeoPoint
    ): Flow<Resource<String>> = callbackFlow {
        trySend(Resource.Loading())

        val currentUserUid = firebaseAuth.currentUser?.uid
        if (currentUserUid == null) {
            trySend(Resource.Error("User not logged in"))
            close()
            return@callbackFlow
        }

        // Fetch driver details from the users collection
        val driverDocRef = firestore.collection("users").document(currentUserUid)
        driverDocRef.get().addOnSuccessListener { documentSnapshot ->
            if (documentSnapshot.exists()) {
                val phone = documentSnapshot.getString("phone") ?: ""
                val name = documentSnapshot.getString("username") ?: ""
                val carNumber = documentSnapshot.getString("carNumber") ?: ""
                val rating = documentSnapshot.getDouble("rating") ?: 4.0
                val carName = documentSnapshot.getString("carName") ?: ""
                val noOfSeats = documentSnapshot.getLong("noOfSeats")?.toInt() ?: 0
                val driverImage = documentSnapshot.getString("image") ?: ""
                preferenceManager.setIsActiveStatus(true)
                // Now that we have the driver details, include them in the trip data
                val tripData = hashMapOf(
                    "driverId" to currentUserUid,
                    "startLocation" to startLocation,
                    "endLocation" to endLocation,
                    "startTime" to startTime,
                    "price" to price,
                    "availableSeats" to noOfSeats,
                    "isActive" to true,
                    "coordinate" to coordinate,
                    "driverPhone" to phone,
                    "driverCarNumber" to carNumber,
                    "driverRating" to rating,
                    "driverCarName" to carName,
                    "driverName" to name,
                    "driverImage" to driverImage
                )

                val tripRef = firestore.collection("trips").document()
                tripRef.set(tripData).addOnSuccessListener {
                    val userTripData = hashMapOf(
                        "currentTripId" to tripRef.id,
                        "isActive" to true
                    )
                    firestore.collection("users").document(currentUserUid)
                        .set(userTripData, SetOptions.merge())
                        .addOnSuccessListener {
                            trySend(Resource.Success("Trip added successfully"))
                        }
                        .addOnFailureListener { e ->
                            trySend(Resource.Error("Failed to update user with trip info: ${e.message}"))
                        }
                }.addOnFailureListener { e ->
                    trySend(Resource.Error("Failed to add trip: ${e.message}"))
                }
            } else {
                trySend(Resource.Error("Driver details not found"))
            }
        }.addOnFailureListener { e ->
            trySend(Resource.Error("Failed to fetch driver details: ${e.message}"))
        }

        awaitClose { close() }
    }

    override fun getCurrentTripForDriver(): Flow<Resource<TripDetail>> = callbackFlow {
        trySend(Resource.Loading())

        val query = firestore.collection("trips")
            .whereEqualTo("driverId", Firebase.auth.currentUser?.uid)
            .whereEqualTo("isActive", true)
            .limit(1) // Assuming there can only be one active trip at a time

        val subscription = query.addSnapshotListener { snapshot, e ->
            if (e != null) {
                trySend(Resource.Error("Error fetching current trip: ${e.message}"))
                return@addSnapshotListener
            }

            val trip = snapshot?.documents?.firstOrNull()?.data
            val tripDetail = TripDetail(
                tripId = snapshot?.documents?.firstOrNull()?.id ?: "trip_id",
                driverId = trip?.get("driverId") as String? ?: "",
                driverCarNumber = trip?.get("driverCarNumber") as String? ?: "",
                driverRating = trip?.get("driverRating") as Double? ?: 4.5,
                driverCarName = trip?.get("driverCarName") as String? ?: "",
                driverName = trip?.get("driverName") as String? ?: "",
                driverImage = trip?.get("driverImage") as String? ?: "",
                driverPhone = trip?.get("driverPhone") as String? ?: "",
                startLocation = trip?.get("startLocation") as String? ?: "",
                endLocation = trip?.get("endLocation") as String? ?: "",
                startTime = trip?.get("startTime") as Long? ?: 0L,
                price = trip?.get("price") as Double? ?: 0.0,
                availableSeats = (trip?.get("availableSeats") as Long?)?.toInt() ?: 0,
                isActive = trip?.get("isActive") as Boolean? ?: true,
                isCancelled = trip?.get("isCancelled") as Boolean? ?: false,
                coordinate = trip?.get("coordinate") as GeoPoint? ?: GeoPoint(
                    0.0,
                    0.0
                )
            )
            if (trip != null) {
                trySend(Resource.Success(tripDetail))
            } else {
                trySend(Resource.Error("No active trip found"))
            }
        }
        awaitClose { subscription.remove() }
    }

    override fun getTripDetails(tripId: String): Flow<Resource<TripDetail>> = callbackFlow {
        trySend(Resource.Loading())

        firestore.collection("trips").document(tripId).get()
            .addOnSuccessListener { document ->
                if (document.exists()) {
                    val data = document.data
                    if (data != null) {
                        val tripDetail = TripDetail(
                            tripId = document.id,
                            driverId = data["driverId"] as String? ?: "",
                            driverCarNumber = data.get("driverCarNumber") as String? ?: "",
                            driverRating = data.get("driverRating") as Double? ?: 4.5,
                            driverCarName = data.get("driverCarName") as String? ?: "",
                            driverName = data.get("driverName") as String? ?: "",
                            driverImage = data.get("driverImage") as String? ?: "",
                            driverPhone = data.get("driverPhone") as String? ?: "",
                            startLocation = data["startLocation"] as String? ?: "",
                            endLocation = data["endLocation"] as String? ?: "",
                            startTime = data["startTime"] as Long? ?: 0L,
                            price = data["price"] as Double? ?: 0.0,
                            availableSeats = data["availableSeats"] as Int? ?: 0,
                            isActive = data["isActive"] as Boolean? ?: true,
                            isCancelled = data["isCancelled"] as Boolean? ?: false,
                            coordinate = data["coordinates"] as GeoPoint? ?: GeoPoint(0.0, 0.0)
                        )
                        trySend(Resource.Success(tripDetail))
                    } else {
                        trySend(Resource.Error("Trip data is null"))
                    }
                } else {
                    trySend(Resource.Error("Trip not found"))
                }
            }
            .addOnFailureListener { e ->
                trySend(Resource.Error("Failed to get trip details: ${e.message}"))
            }

        awaitClose { close() }
    }

    override fun markTripAsCompleted(tripId: String): Flow<Resource<String>> = callbackFlow {
        trySend(Resource.Loading())
        preferenceManager.setIsActiveStatus(false)

        val tripUpdate = hashMapOf(
            "isActive" to false,
            "isCompleted" to true
        )
        val userUpdate = hashMapOf(
            "isActive" to false
        )
        firestore.collection("trips").document(tripId)
            .update(tripUpdate as Map<String, Any>)
            .addOnSuccessListener {
                firestore.collection("users").document(Firebase.auth.currentUser?.uid!!)
                    .update(userUpdate as Map<String, Any>).addOnSuccessListener {
                        trySend(Resource.Success("Trip marked as completed"))
                    }.addOnFailureListener { e ->
                        trySend(Resource.Error("Failed to mark user as inActive: ${e.message}"))
                    }
            }
            .addOnFailureListener { e ->
                trySend(Resource.Error("Failed to mark trip as completed: ${e.message}"))
            }

        awaitClose { close() }
    }

    override fun cancelTrip(tripId: String): Flow<Resource<String>> = callbackFlow {
        trySend(Resource.Loading())
        preferenceManager.setIsActiveStatus(false)
        val tripUpdate = hashMapOf(
            "isActive" to false,
            "isCancelled" to true
        )
        val userUpdate = hashMapOf(
            "isActive" to false
        )
        firestore.collection("trips").document(tripId)
            .update(tripUpdate as Map<String, Any>)
            .addOnSuccessListener {
                firestore.collection("users").document(Firebase.auth.currentUser?.uid!!)
                    .update(userUpdate as Map<String, Any>).addOnSuccessListener {
                        trySend(Resource.Success("Trip marked as cancelled"))
                    }.addOnFailureListener { e ->
                        trySend(Resource.Error("Failed to mark user as inActive: ${e.message}"))
                    }
            }
            .addOnFailureListener { e ->
                trySend(Resource.Error("Failed to cancel trip: ${e.message}"))
            }

        awaitClose { close() }
    }

    override fun updateDriverLocation(
        driverId: String,
        newLocation: GeoPoint
    ): Flow<Resource<Void>> = callbackFlow {
        trySend(Resource.Loading())

        val query = firestore.collection("trips")
            .whereEqualTo("driverId", driverId)
            .whereEqualTo("isActive", true)
            .limit(1)

        query.get().addOnSuccessListener { querySnapshot ->
            if (!querySnapshot.isEmpty) {
                val documentSnapshot = querySnapshot.documents.first()
                val tripId = documentSnapshot.id

                firestore.collection("trips").document(tripId)
                    .update("coordinate", newLocation)
                    .addOnSuccessListener {
                        trySend(Resource.Success(null))
                    }
                    .addOnFailureListener { e ->
                        trySend(Resource.Error("Failed to update driver location: ${e.message}"))
                    }
            } else {
                trySend(Resource.Error("No active trip found for driver"))
            }
        }.addOnFailureListener { e ->
            trySend(Resource.Error("Failed to find active trip for driver: ${e.message}"))
        }

        awaitClose { close() }
    }

    override fun getAllPastTrips(userId: String): Flow<Resource<List<TripDetail>>> = callbackFlow {
        trySend(Resource.Loading())

        firestore.collection("trips")
            .whereEqualTo("driverId", userId)
            .whereEqualTo("isActive", false)
            .get()
            .addOnSuccessListener { queryDocumentSnapshots ->
                val pastTrips = queryDocumentSnapshots.documents.mapNotNull { document ->
                    document.toObject(TripDetail::class.java)
                }
                trySend(Resource.Success(pastTrips))
            }
            .addOnFailureListener { e ->
                trySend(Resource.Error("Failed to get past trips: ${e.message}"))
            }

        awaitClose { close() }
    }

    fun listenForTripUpdates(tripId: String): Flow<Resource<TripDetail>> = callbackFlow {
        trySend(Resource.Loading())

        val tripRef = FirebaseFirestore.getInstance().collection("trips").document(tripId)

        // Listen for real-time updates to the trip document
        val subscription = tripRef.addSnapshotListener { snapshot, e ->
            if (e != null) {
                trySend(Resource.Error("Failed to listen for trip updates: ${e.message}"))
                return@addSnapshotListener
            }

            if (snapshot != null && snapshot.exists()) {
                val tripDetail = snapshot.toObject<TripDetail>()
                if (tripDetail != null) {
                    trySend(Resource.Success(tripDetail))
                } else {
                    trySend(Resource.Error("Error parsing trip details"))
                }
            } else {
                trySend(Resource.Error("Trip not found"))
            }
        }

        // Await close and remove the snapshot listener when the flow is no longer collected
        awaitClose {
            subscription.remove()
        }
    }

}