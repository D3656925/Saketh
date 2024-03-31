package uk.ac.tees.mad.d3656925.data

import com.google.firebase.auth.AuthResult
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.tasks.await
import uk.ac.tees.mad.d3656925.domain.Resource
import javax.inject.Inject

interface CommuterCarpoolingRepository {
    fun loginUser(email: String, password: String): Flow<Resource<AuthResult>>
    fun registerUser(email: String, password: String, username: String): Flow<Resource<AuthResult>>
    fun forgotPassword(email: String): Flow<Resource<Boolean>>
    suspend fun saveUser(email: String?, username: String?, userId: String?)
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
}