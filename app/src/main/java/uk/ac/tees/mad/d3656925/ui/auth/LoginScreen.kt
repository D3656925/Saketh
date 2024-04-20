package uk.ac.tees.mad.d3656925.ui.auth

import android.widget.Toast
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material.icons.filled.VisibilityOff
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusDirection
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.RectangleShape
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import kotlinx.coroutines.launch
import uk.ac.tees.mad.d3656925.R
import uk.ac.tees.mad.d3656925.navigation.NavigationDestination


object LoginDestination : NavigationDestination {
    override val routeName: String
        get() = "login"
    override val titleResource: Int
        get() = R.string.login
}

@Composable
fun LoginScreen(
    viewModel: LoginViewModel = hiltViewModel(),
    loginSuccess: () -> Unit,
    onNavigateUp: () -> Unit,
    onRegisterClick: () -> Unit,
    onForgetPassword: () -> Unit
    ) {
        val signInStatus = viewModel.signInState.collectAsState(initial = null)
        val signInState = viewModel.state.collectAsState().value
        val loginUiState = viewModel.loginUiState.collectAsState().value
        val focusManager = LocalFocusManager.current
        var passwordVisible by rememberSaveable { mutableStateOf(false) }
        val scope = rememberCoroutineScope()
        val context = LocalContext.current

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
            .background(Color.White)
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically
        ) {
            IconButton(onClick = onNavigateUp) {
                Icon(
                    imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                    contentDescription = "Back"
                )
            }
            Text(text = "Continue with email", fontSize = 20.sp)
        }
        Spacer(modifier = Modifier.height(20.dp))
        Text(text = "Login to take the best car pooling experience. ", fontSize = 14.sp)
        Spacer(modifier = Modifier.height(20.dp))
        Text(text = "Email address", fontSize = 14.sp, fontWeight = FontWeight.Bold)

        OutlinedTextField(
            value = loginUiState.email,
            onValueChange = {
                viewModel.updateLoginState(loginUiState.copy(email = it))
            },
            modifier = Modifier.fillMaxWidth(),
            placeholder = {
                Text(text = "Email")
            },
            maxLines = 1,
            visualTransformation = VisualTransformation.None,
            keyboardOptions = KeyboardOptions(imeAction = ImeAction.Next),
            keyboardActions = KeyboardActions(onNext = {
                focusManager.moveFocus(FocusDirection.Down)
            }),
        )
        Spacer(modifier = Modifier.height(20.dp))

        Text(text = "Password", fontSize = 14.sp, fontWeight = FontWeight.Bold)

        OutlinedTextField(
            value = loginUiState.password,
            onValueChange = {
                viewModel.updateLoginState(loginUiState.copy(password = it))
            },

            modifier = Modifier.fillMaxWidth(),

            maxLines = 1,
            trailingIcon = {
                val image = if (passwordVisible)
                    Icons.Default.Visibility
                else Icons.Filled.VisibilityOff

                val description =
                    if (passwordVisible) "Hide password" else "Show password"

                IconButton(onClick = { passwordVisible = !passwordVisible }) {
                    Icon(
                        imageVector = image,
                        description,
                    )
                }
            },
            placeholder = {
                Text(text = "Password")
            },
            visualTransformation = if (passwordVisible) VisualTransformation.None else PasswordVisualTransformation(),
            keyboardOptions = KeyboardOptions(
                imeAction = ImeAction.Done,
            ),
            keyboardActions = KeyboardActions(onDone = {
                focusManager.clearFocus()
            })
        )
        Spacer(modifier = Modifier.height(12.dp))
        Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
            Text(
                text = "Create new account",
                textDecoration = TextDecoration.Underline,
                modifier = Modifier.clickable {
                    onRegisterClick()
                }
            )
            Text(
                text = "Forgot password?",
                textDecoration = TextDecoration.Underline,
                modifier = Modifier.clickable {
                    onForgetPassword()
                }
            )
        }

        Spacer(modifier = Modifier.height(20.dp))

        Button(
            onClick = { viewModel.loginUser(loginUiState.email, loginUiState.password) },
            modifier = Modifier
                .fillMaxWidth()
                .height(50.dp),
            shape = RectangleShape
        ) {
            if (signInStatus.value?.isLoading == true) {
                CircularProgressIndicator(color = MaterialTheme.colorScheme.background)
            } else {
                Text(text = "Log in", fontSize = 20.sp)
            }
        }
        LaunchedEffect(key1 = signInStatus.value?.isSuccess) {
            scope.launch {
                if (signInStatus.value?.isSuccess?.isNotEmpty() == true) {
                    focusManager.clearFocus()
                    val success = signInStatus.value?.isSuccess
                    Toast.makeText(context, "$success", Toast.LENGTH_LONG).show()
                    loginSuccess()
                }
            }
        }

        LaunchedEffect(key1 = signInStatus.value?.isError) {
            scope.launch {
                if (signInStatus.value?.isError?.isNotEmpty() == true) {
                    val error = signInStatus.value?.isError
                    Toast.makeText(context, "$error", Toast.LENGTH_LONG).show()
                }
            }
        }
        LaunchedEffect(key1 = signInState.signInError) {
            scope.launch {
                if (signInState.signInError?.isNotEmpty() == true) {
                    val error = signInState.signInError
                    Toast.makeText(context, "$error", Toast.LENGTH_LONG).show()
                }
            }
        }
    }
}