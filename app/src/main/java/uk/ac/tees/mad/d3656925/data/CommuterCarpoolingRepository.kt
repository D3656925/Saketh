package uk.ac.tees.mad.d3656925.data

import android.util.Log
import com.google.firebase.Firebase
import com.google.firebase.auth.AuthResult
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.SetOptions
import com.google.firebase.storage.storage
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.tasks.await
import uk.ac.tees.mad.d3656925.domain.Resource
import uk.ac.tees.mad.d3656925.domain.UserResponse
import java.util.UUID
import javax.inject.Inject

interface CommuterCarpoolingRepository {
    fun loginUser(email: String, password: String): Flow<Resource<AuthResult>>
    fun registerUser(email: String, password: String, username: String): Flow<Resource<AuthResult>>
    fun forgotPassword(email: String): Flow<Resource<Boolean>>
    suspend fun saveUser(email: String?, username: String?, userId: String?)
    fun getCurrentUser(): Flow<Resource<UserResponse>>
    fun updateCurrentUser(
        image: ByteArray, phone: String, carNumber: String, rating: Double
    ): Flow<Resource<String>>
}

class CommuterCarpoolingRepositoryImpl @Inject constructor(
    private val firebaseAuth: FirebaseAuth,
    private val firebaseFirestore: FirebaseFirestore
) : CommuterCarpoolingRepository {

    override fun loginUser(email: String, password: String): Flow<Resource<AuthResult>> {
        return flow {
            emit(Resource.Loading())
            val result = firebaseAuth.signInWithEmailAndPassword(email, password).await()
            emit(Resource.Success(result))
        }.catch {
            emit(Resource.Error(it.message.toString()))
        }
    }

    override fun registerUser(
        email: String,
        password: String,
        username: String
    ): Flow<Resource<AuthResult>> {
        return flow {
            emit(Resource.Loading())
            val authResult = firebaseAuth.createUserWithEmailAndPassword(email, password).await()
            // Add user to Firestore with username
            val userId = authResult.user?.uid
            saveUser(userId = userId, email = email, username = username)
            emit(Resource.Success(authResult))
        }.catch {
            emit(Resource.Error(it.message.toString()))
        }
    }

    override fun forgotPassword(email: String): Flow<Resource<Boolean>> {
        return flow {
            emit(Resource.Loading())

            firebaseAuth.sendPasswordResetEmail(email).await()

            emit(Resource.Success(true))
        }.catch {
            emit(Resource.Error(it.message.toString()))
        }
    }

    override suspend fun saveUser(email: String?, username: String?, userId: String?) {
        if (userId != null) {
            val userMap = hashMapOf(
                "email" to email,
                "username" to username
                // Add other user data if needed
            )
            firebaseFirestore.collection("users").document(userId).set(userMap).await()
        }
    }


    override fun getCurrentUser(): Flow<Resource<UserResponse>> = callbackFlow {
        trySend(Resource.Loading())
        val currentUserUid = firebaseAuth.currentUser?.uid
        if (currentUserUid != null) {
            firebaseFirestore.collection("users").document(currentUserUid).get()
                .addOnSuccessListener { mySnapshot ->
                    if (mySnapshot.exists()) {
                        val data = mySnapshot.data

                        if (data != null) {
                            val userResponse = UserResponse(
                                key = currentUserUid,
                                item = UserResponse.CurrentUser(
                                    name = data["username"] as String? ?: "",
                                    email = data["email"] as String? ?: "",
                                    phone = data["phone"] as String? ?: "",
                                    profileImage = data["images"] as String? ?: ""
                                )
                            )

                            trySend(Resource.Success(userResponse))
                        } else {
                            trySend(Resource.Error(message = "No data found in Database"))

                            println("No data found in Database")
                        }
                    } else {
                        trySend(Resource.Error(message = "No data found in Database"))
                        println("No data found in Database")
                    }
                }.addOnFailureListener { e ->
                    Log.d("ERRor", e.toString())
                    trySend(Resource.Error(message = e.toString()))
                }
        } else {
            trySend(Resource.Error(message = "User not signed up"))
        }
        awaitClose {
            close()
        }
    }


    override fun updateCurrentUser(
        image: ByteArray,
        phone: String,
        carNumber: String,
        rating: Double
    ): Flow<Resource<String>> =
        callbackFlow {
            trySend(Resource.Loading())


            val storageRef = Firebase.storage.reference
            val uuid = UUID.randomUUID()
            val imagesRef = storageRef.child("images/$uuid")
            val currentUserUid = firebaseAuth.currentUser?.uid

            val uploadTask =
                image.let {
                    imagesRef.putBytes(it)
                }

            uploadTask.addOnSuccessListener {
                imagesRef.downloadUrl
                    .addOnSuccessListener { uri ->
                        val map = HashMap<String, Any>()
                        map["phone"] = phone
                        map["image"] = uri.toString()
                        map["carNumber"] = carNumber
                        map["rating"] = rating

                        if (currentUserUid != null) {
                            firebaseFirestore.collection("users")
                                .document(currentUserUid)
                                .set(map, SetOptions.merge())
                                .addOnCompleteListener {
                                    if (it.isSuccessful) {
                                        trySend(Resource.Success("Updated Successfully.."))
                                    }
                                }
                                .addOnFailureListener { e ->
                                    trySend(Resource.Error(message = e.message))
                                }
                        } else {
                            trySend(Resource.Error(message = "User not logged in"))
                        }
                    }
                    .addOnFailureListener {
                        trySend(Resource.Error(message = "Updating user failed Successfully: $it"))
                    }
            }.addOnFailureListener {
                trySend(Resource.Error(message = "Image upload failed Successfully: $it"))
            }
            awaitClose { close() }
        }


}