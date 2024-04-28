package uk.ac.tees.mad.d3656925.data

import android.content.Context
import com.google.firebase.Firebase
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.auth
import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.GeoPoint
import com.google.firebase.firestore.SetOptions
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
    fun getAllPastTrips(): Flow<Resource<List<TripDetail>>>
    fun updateDriverLocation(driverId: String, newLocation: GeoPoint): Flow<Resource<Void>>
    fun getAllTripsForPassengers(): Flow<Resource<List<TripDetail>>>
    fun joinTrip(tripId: String, userId: String): Flow<Resource<String>>
    fun leaveTrip(tripId: String, userId: String): Flow<Resource<String>>
    fun hasUserJoinedTrip(tripId: String, userId: String): Flow<Resource<Boolean>>
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
                            availableSeats = (data["availableSeats"] as Long? ?: 0).toInt(),
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

    override fun getAllTripsForPassengers(): Flow<Resource<List<TripDetail>>> = callbackFlow {
        trySend(Resource.Loading())

        val query = firestore.collection("trips")
            .whereEqualTo("isActive", true)
            .whereEqualTo("isCancelled", false)

        val subscription = query.addSnapshotListener { snapshot, e ->
            if (e != null) {
                trySend(Resource.Error("Error fetching trips: ${e.message}"))
                return@addSnapshotListener
            }

            val trips = snapshot?.documents?.mapNotNull { document ->
                val data = document.data
                TripDetail(
                    tripId = document.id,
                    driverId = data?.get("driverId") as String? ?: "",
                    driverCarNumber = data?.get("driverCarNumber") as String? ?: "",
                    driverRating = data?.get("driverRating") as Double? ?: 4.5,
                    driverCarName = data?.get("driverCarName") as String? ?: "",
                    driverName = data?.get("driverName") as String? ?: "",
                    driverImage = data?.get("driverImage") as String? ?: "",
                    driverPhone = data?.get("driverPhone") as String? ?: "",
                    startLocation = data?.get("startLocation") as String? ?: "",
                    endLocation = data?.get("endLocation") as String? ?: "",
                    startTime = data?.get("startTime") as Long? ?: 0L,
                    price = data?.get("price") as Double? ?: 0.0,
                    availableSeats = (data?.get("availableSeats") as Long?)?.toInt() ?: 0,
                    isActive = data?.get("isActive") as Boolean? ?: true,
                    isCancelled = data?.get("isCancelled") as Boolean? ?: false,
                    coordinate = data?.get("coordinate") as GeoPoint? ?: GeoPoint(0.0, 0.0)
                )
            } ?: emptyList()

            trySend(Resource.Success(trips))
        }

        // Clean up when the flow collection is cancelled or no longer in use
        awaitClose { subscription.remove() }
    }

    override fun joinTrip(tripId: String, userId: String): Flow<Resource<String>> = callbackFlow {
        trySend(Resource.Loading())

        val tripRef = firestore.collection("trips").document(tripId)

        firestore.runTransaction { transaction ->
            val snapshot = transaction.get(tripRef)
            val currentSeats = snapshot.getLong("availableSeats") ?: 0
            if (currentSeats > 0) {
                transaction.update(tripRef, "availableSeats", currentSeats - 1)
                transaction.update(tripRef, "passengers", FieldValue.arrayUnion(userId))
                Resource.Success("Joined trip successfully")
            } else {
                throw Exception("No available seats")
            }
        }.addOnSuccessListener {
            trySend(it as Resource.Success<String>)
        }.addOnFailureListener { e ->
            trySend(Resource.Error("Failed to join trip: ${e.message}"))
        }

        awaitClose { close() }
    }

    override fun leaveTrip(tripId: String, userId: String): Flow<Resource<String>> = callbackFlow {
        trySend(Resource.Loading())

        val tripRef = firestore.collection("trips").document(tripId)

        firestore.runTransaction { transaction ->
            val snapshot = transaction.get(tripRef)
            val currentSeats = snapshot.getLong("availableSeats") ?: 0
            transaction.update(tripRef, "availableSeats", currentSeats + 1)
            // Remove the user from the list of passengers
            transaction.update(tripRef, "passengers", FieldValue.arrayRemove(userId))
            Resource.Success("Left trip successfully")
        }.addOnSuccessListener {
            trySend(it as Resource.Success<String>)
        }.addOnFailureListener { e ->
            trySend(Resource.Error("Failed to leave trip: ${e.message}"))
        }

        awaitClose { close() }
    }


    override fun getAllPastTrips(): Flow<Resource<List<TripDetail>>> = callbackFlow {
        trySend(Resource.Loading())

        val currentUserUid = FirebaseAuth.getInstance().currentUser?.uid
        if (currentUserUid == null) {
            trySend(Resource.Error("User not logged in"))
            close()
            return@callbackFlow
        }

        firestore.collection("trips")
            .whereArrayContains("passengers", currentUserUid)
            .whereEqualTo("isCompleted", true)
            .get()
            .addOnSuccessListener { queryDocumentSnapshots ->
                val pastTrips = queryDocumentSnapshots?.documents?.mapNotNull { document ->
                    val data = document.data
                    TripDetail(
                        tripId = document.id,
                        driverId = data?.get("driverId") as String? ?: "",
                        driverCarNumber = data?.get("driverCarNumber") as String? ?: "",
                        driverRating = data?.get("driverRating") as Double? ?: 4.5,
                        driverCarName = data?.get("driverCarName") as String? ?: "",
                        driverName = data?.get("driverName") as String? ?: "",
                        driverImage = data?.get("driverImage") as String? ?: "",
                        driverPhone = data?.get("driverPhone") as String? ?: "",
                        startLocation = data?.get("startLocation") as String? ?: "",
                        endLocation = data?.get("endLocation") as String? ?: "",
                        startTime = data?.get("startTime") as Long? ?: 0L,
                        price = data?.get("price") as Double? ?: 0.0,
                        availableSeats = (data?.get("availableSeats") as Long?)?.toInt() ?: 0,
                        isActive = data?.get("isActive") as Boolean? ?: true,
                        isCancelled = data?.get("isCancelled") as Boolean? ?: false,
                        coordinate = data?.get("coordinate") as GeoPoint? ?: GeoPoint(0.0, 0.0)
                    )
                } ?: emptyList()
                trySend(Resource.Success(pastTrips))
            }
            .addOnFailureListener { exception ->
                trySend(Resource.Error("Failed to fetch past trips: ${exception.message}"))
            }

        awaitClose { close() }
    }

    override fun hasUserJoinedTrip(tripId: String, userId: String): Flow<Resource<Boolean>> =
        callbackFlow {
            trySend(Resource.Loading())

            val tripRef = firestore.collection("trips").document(tripId)

            tripRef.get().addOnSuccessListener { documentSnapshot ->
                if (documentSnapshot.exists()) {
                    val passengers =
                        documentSnapshot.get("passengers") as? List<String> ?: emptyList()
                    if (userId in passengers) {
                        trySend(Resource.Success(true))
                    } else {
                        trySend(Resource.Success(false))
                    }
                } else {
                    trySend(Resource.Error("Trip not found"))
                }
            }.addOnFailureListener { e ->
                trySend(Resource.Error("Failed to check if user joined trip: ${e.message}"))
            }

            awaitClose { close() }
        }
}