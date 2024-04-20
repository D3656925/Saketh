package uk.ac.tees.mad.d3656925.ui.auth

import android.app.Activity
import android.util.Log
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.IntentSenderRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.outlined.Email
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import com.google.android.gms.auth.api.identity.Identity
import kotlinx.coroutines.launch
import uk.ac.tees.mad.d3656925.R
import uk.ac.tees.mad.d3656925.navigation.NavigationDestination


object AskLoginDestination : NavigationDestination {
    override val routeName: String
        get() = "ask_login"
    override val titleResource: Int
        get() = R.string.ask_login
}

@Composable
fun AskLogin(
    viewModel: LoginViewModel = hiltViewModel(),
    loginSuccess: () -> Unit,
    onLoginClick: () -> Unit,
    navigateUp: () -> Unit,
    askForDetails: () -> Unit,
) {
    val signInState = viewModel.state.collectAsState().value
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    val googleAuthUiClient by lazy {
        GoogleAuthUiClient(
            oneTapClient = Identity.getSignInClient(context)
        )
    }
    val currentUserStatus = viewModel.currentUserStatus.collectAsState(initial = null)
    var isGoogleSigned by remember {
        mutableStateOf(false)
    }
    val launcher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.StartIntentSenderForResult(),
        onResult = { result ->
            if (result.resultCode == Activity.RESULT_OK) {
                scope.launch {
                    val signInResult = googleAuthUiClient.signInWithIntent(
                        intent = result.data ?: return@launch
                    )
                    viewModel.onSignInWithGoogleResult(signInResult)
                }
            }
        }
    )
    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.White)
    ) {
        Column(
            Modifier
                .padding(16.dp)
                .fillMaxSize()
                .border(BorderStroke(3.dp, Color.Gray.copy(0.5f)))
        ) {

            IconButton(onClick = navigateUp) {
                Icon(
                    imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                    contentDescription = "Back"
                )
            }
            Column(
                Modifier
                    .padding(horizontal = 16.dp)
                    .weight(1f)
            ) {
                Spacer(modifier = Modifier.height(30.dp))
                Text(text = "Welcome!", fontSize = 28.sp, fontWeight = FontWeight.Bold)
                Spacer(modifier = Modifier.height(16.dp))
                Text(
                    text = "We are so glad you're here! Please choose an option below to Sign in.",
                    color = Color.Gray,
                    fontWeight = FontWeight.Bold,
                    fontSize = 16.sp
                )
                Box(modifier = Modifier.weight(1f)) {
                }
                Box(modifier = Modifier.weight(2f)) {
                    Box(
                        modifier = Modifier
                            .padding(start = 14.dp, top = 8.dp)
                            .clip(RoundedCornerShape(16.dp))
                            .height(150.dp)
                            .width(200.dp)
                            .background(Color.Magenta.copy(alpha = 0.3f))
                    )
                    Row(
                        Modifier
                            .fillMaxWidth()
                            .padding(top = 90.dp, end = 20.dp),
                        horizontalArrangement = Arrangement.End
                    ) {
                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(16.dp))
                                .height(180.dp)
                                .width(240.dp)
                                .background(Color.Red.copy(alpha = 0.3f))
                        )
                    }
                    Box(modifier = Modifier.fillMaxSize()) {
                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(16.dp))
                                .height(200.dp)
                                .width(265.dp)
                                .align(Alignment.Center)
                        ) {
                            Image(
                                painter = painterResource(id = R.drawable.car_pooling),
                                contentDescription = "",
                                contentScale = ContentScale.Crop,
                                modifier = Modifier.fillMaxSize()
                            )
                        }
                    }


                }
                Box(modifier = Modifier.weight(1f)) {
                }
            }
            HorizontalDivider(color = Color.Gray.copy(0.5f), thickness = 3.dp)

            Row(
                Modifier
                    .height(60.dp)
                    .padding(horizontal = 16.dp)
                    .clickable {
                        scope.launch {
                            val signInIntentSender = googleAuthUiClient.signIn()
                            launcher.launch(
                                IntentSenderRequest
                                    .Builder(
                                        signInIntentSender ?: return@launch
                                    )
                                    .build()
                            )

                        }
                    },
                verticalAlignment = Alignment.CenterVertically
            ) {
                Image(
                    painter = painterResource(id = R.drawable.ic_google),
                    contentDescription = "",
                    modifier = Modifier.size(25.dp),
                )
                Row(Modifier.weight(1f), horizontalArrangement = Arrangement.Center) {
                    Text(text = "Continue with Google", fontWeight = FontWeight.Bold)
                }
            }

            HorizontalDivider(color = Color.Gray.copy(0.5f), thickness = 3.dp)

            Row(
                Modifier
                    .height(60.dp)
                    .padding(horizontal = 16.dp)
                    .clickable {
                        onLoginClick()
                    },
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(
                    imageVector = Icons.Outlined.Email, contentDescription = "",
                    modifier = Modifier.size(25.dp)
                )
                Row(Modifier.weight(1f), horizontalArrangement = Arrangement.Center) {
                    Text(text = "Continue with email", fontWeight = FontWeight.Bold)
                }
            }
        }
        LaunchedEffect(key1 = signInState.isSignInSuccessful) {
            if (signInState.isSignInSuccessful) {
                Toast.makeText(
                    context,
                    "Sign in successful",
                    Toast.LENGTH_LONG
                ).show()
                viewModel.getUserDetails()
                isGoogleSigned = true
            }
        }

        LaunchedEffect(currentUserStatus.value?.isSuccess) {
            if (currentUserStatus.value?.isSuccess != null && isGoogleSigned) {
                if (currentUserStatus.value?.isSuccess?.item?.phone.isNullOrEmpty()) {
                    val user = googleAuthUiClient.getSignedInUser()
                    if (user != null) {
                        Log.d("USER", currentUserStatus.value!!.isSuccess.toString())
                        askForDetails()
                    }
                } else {
                    loginSuccess()
                }
            }
        }

        LaunchedEffect(currentUserStatus.value?.isError) {
            if (currentUserStatus.value?.isError != null && isGoogleSigned) {
                val user = googleAuthUiClient.getSignedInUser()
                viewModel.saveUserInFirestore(user!!)
                askForDetails()
            }
        }
    }
}