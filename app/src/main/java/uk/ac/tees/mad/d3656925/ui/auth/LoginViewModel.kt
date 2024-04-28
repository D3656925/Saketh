package uk.ac.tees.mad.d3656925.ui.auth

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.google.firebase.auth.FirebaseAuth
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import uk.ac.tees.mad.d3656925.data.CommuterCarpoolingRepository
import uk.ac.tees.mad.d3656925.domain.LoginState
import uk.ac.tees.mad.d3656925.domain.LoginStatus
import uk.ac.tees.mad.d3656925.domain.RegisterState
import uk.ac.tees.mad.d3656925.domain.Resource
import uk.ac.tees.mad.d3656925.domain.SignInResult
import uk.ac.tees.mad.d3656925.domain.UserData
import uk.ac.tees.mad.d3656925.domain.UserResponse
import javax.inject.Inject

@HiltViewModel
class LoginViewModel @Inject constructor(
    private val repository: CommuterCarpoolingRepository,
    firebaseAuth: FirebaseAuth
) : ViewModel() {

    private val _loginUiState = MutableStateFlow(LoginUiState())
    val loginUiState = _loginUiState.asStateFlow()

    private val _state = MutableStateFlow(LoginState())
    val state = _state.asStateFlow()

    private val _signInStatus = Channel<LoginStatus>()
    val signInState = _signInStatus.receiveAsFlow()

    private val _googleSignInResult = MutableStateFlow(SignInResult())
    val googleSignInResult = _googleSignInResult.asStateFlow()

    private val _signupUiState = MutableStateFlow(SignUpUiState())
    val signUpUiState = _signupUiState.asStateFlow()

    private val _signUpState = Channel<RegisterState>()
    val signUpState = _signUpState.receiveAsFlow()

    private val _currentUserStatus = Channel<CurrentUser>()
    val currentUserStatus = _currentUserStatus.receiveAsFlow()

    private val _updateDetailStatus = Channel<LoginStatus>()
    val updateDetailsStatus = _updateDetailStatus.receiveAsFlow()

    fun resetState() {
        _state.update { LoginState() }
        _googleSignInResult.update { SignInResult() }
    }

    fun updateLoginState(value: LoginUiState) {
        _loginUiState.value = value
    }

    fun onSignInWithGoogleResult(result: SignInResult) {
        _state.update {
            it.copy(
                isSignInSuccessful = result.data != null, signInError = result.errorMessage
            )
        }
    }

    fun loginUser(email: String, password: String) = viewModelScope.launch {
        repository.loginUser(email, password).collect { result ->
            when (result) {
                is Resource.Error -> {
                    _signInStatus.send(LoginStatus(isError = result.message))
                }

                is Resource.Loading -> {
                    _signInStatus.send(LoginStatus(isLoading = true))
                }

                is Resource.Success -> {
                    _signInStatus.send(LoginStatus(isSuccess = "Sign In Success"))

                }
            }
        }
    }

    fun forgotPassword(email: String) = viewModelScope.launch {
        repository.forgotPassword(email).collect { result ->
            when (result) {
                is Resource.Error -> {
                    _signInStatus.send(LoginStatus(isError = result.message))
                }

                is Resource.Loading -> {
                    _signInStatus.send(LoginStatus(isLoading = true))
                }

                is Resource.Success -> {
                    _signInStatus.send(LoginStatus(isSuccess = "Sent forget password email."))

                }
            }
        }
    }

    fun saveUserInFirestore(user: UserData) = viewModelScope.launch {
        repository.saveUser(email = user.email, username = user.username, userId = user.userId)
    }

    init {
        getUserDetails()
    }

    fun getUserDetails() =
        viewModelScope.launch {

            repository.getCurrentUser().collect { result ->
                when (result) {
                    is Resource.Error -> {
                        _currentUserStatus.send(CurrentUser(isError = result.message))
                    }

                    is Resource.Loading -> {
                        _currentUserStatus.send(CurrentUser(isLoading = true))
                    }

                    is Resource.Success -> {
                        _currentUserStatus.send(CurrentUser(isSuccess = result.data))

                    }
                }
            }
        }

    fun updateSignUpState(value: SignUpUiState) {
        _signupUiState.value = value
    }

    fun registerUser(email: String, password: String, username: String) = viewModelScope.launch {
        repository.registerUser(email, password, username).collect { result ->
            when (result) {
                is Resource.Error -> {
                    _signUpState.send(RegisterState(isError = result.message))
                }

                is Resource.Loading -> {
                    _signUpState.send(RegisterState(isLoading = true))
                }

                is Resource.Success -> {
                    _signUpState.send(RegisterState(isSuccess = "Register Success"))
                }
            }
        }
    }


    suspend fun addUserDetail(
        image: ByteArray,
        phone: String,
        carNumber: String,
        rating: Double,
        carName: String,
        noOfSeats: String,

    ) {
        repository.updateCurrentUser(image, phone, carNumber, rating, carName, noOfSeats).collect {
            when (it) {
                is Resource.Error -> {
                    _updateDetailStatus.send(LoginStatus(isError = it.message))
                }

                is Resource.Loading -> {
                    _updateDetailStatus.send(LoginStatus(isLoading = true))

                }

                is Resource.Success -> {
                    _updateDetailStatus.send(LoginStatus(isSuccess = it.data))
                }
            }
        }
    }

}

data class LoginUiState(
    val email: String = "", val password: String = ""
)

data class SignUpUiState(
    val name: String = "", val email: String = "", val password: String = ""
)


data class CurrentUser(
    val isLoading: Boolean = false,
    val isSuccess: UserResponse? = null,
    val isError: String? = null
)