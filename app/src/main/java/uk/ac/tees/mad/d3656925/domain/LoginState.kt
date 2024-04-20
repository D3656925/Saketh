package uk.ac.tees.mad.d3656925.domain

data class LoginState(
    val isSignInSuccessful: Boolean = false,
    val signInError: String? = null
)

data class LoginStatus(
    val isLoading: Boolean = false,
    val isSuccess: String? = null,
    val isError: String? = null
)

data class UserData(
    val userId: String,
    val username: String?,
    val email: String?
)
data class SignInResult(
    val data: UserData? = null,
    val isSuccessful: Boolean = false,
    val errorMessage: String? = null
)