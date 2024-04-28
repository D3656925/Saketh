package uk.ac.tees.mad.d3656925.navigation

import androidx.compose.runtime.Composable
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.platform.LocalContext
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.google.android.gms.auth.api.identity.Identity
import com.google.firebase.auth.FirebaseAuth
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import uk.ac.tees.mad.d3656925.ui.auth.AskLogin
import uk.ac.tees.mad.d3656925.ui.auth.AskLoginDestination
import uk.ac.tees.mad.d3656925.ui.auth.ForgotPasswordDestination
import uk.ac.tees.mad.d3656925.ui.auth.ForgotPasswordScreen
import uk.ac.tees.mad.d3656925.ui.auth.GoogleAuthUiClient
import uk.ac.tees.mad.d3656925.ui.auth.LoginDestination
import uk.ac.tees.mad.d3656925.ui.auth.LoginScreen
import uk.ac.tees.mad.d3656925.ui.auth.RegisterDestination
import uk.ac.tees.mad.d3656925.ui.auth.RegisterScreen
import uk.ac.tees.mad.d3656925.ui.auth.UserDetails
import uk.ac.tees.mad.d3656925.ui.auth.UserDetailsDestination
import uk.ac.tees.mad.d3656925.ui.screens.DriverDestination
import uk.ac.tees.mad.d3656925.ui.screens.DriverScreen
import uk.ac.tees.mad.d3656925.ui.screens.EditProfileDestination
import uk.ac.tees.mad.d3656925.ui.screens.EditProfileScreen
import uk.ac.tees.mad.d3656925.ui.screens.PassengerDestination
import uk.ac.tees.mad.d3656925.ui.screens.PassengerScreen
import uk.ac.tees.mad.d3656925.ui.screens.PastCarpoolsDestination
import uk.ac.tees.mad.d3656925.ui.screens.PastCarpoolsScreen
import uk.ac.tees.mad.d3656925.ui.screens.ProfileDestination
import uk.ac.tees.mad.d3656925.ui.screens.ProfileScreen
import uk.ac.tees.mad.d3656925.ui.screens.SplashDestination
import uk.ac.tees.mad.d3656925.ui.screens.SplashScreen
import uk.ac.tees.mad.d3656925.ui.viewmodels.DriverViewModel
import uk.ac.tees.mad.d3656925.utils.location.ApplicationViewModel

@Composable
fun CarpoolingApp() {
    // Create a NavHostController to manage navigation within the app
    val navController = rememberNavController()
    val scope = rememberCoroutineScope()
    val firebase = FirebaseAuth.getInstance()
    val currentUser = firebase.currentUser
    val context = LocalContext.current

    val driverViewModel: DriverViewModel = hiltViewModel()
    val applicationViewModel: ApplicationViewModel = hiltViewModel()

    val googleAuthUiClient by lazy {
        GoogleAuthUiClient(
            oneTapClient = Identity.getSignInClient(context)
        )
    }
    val initialDestination =
        if ((currentUser != null) || (googleAuthUiClient.getSignedInUser() != null)) {
            DriverDestination.routeName
        } else {
            AskLoginDestination.routeName
        }

    // Define the navigation graph using NavHost
    NavHost(navController = navController, startDestination = SplashDestination.routeName) {
        // Splash screen destination
        composable(SplashDestination.routeName) {
            // Display the splash screen composable
            SplashScreen(onFinish = {
                scope.launch(Dispatchers.Main) {
                    navController.popBackStack() // Clear any existing back stack
                    navController.navigate(initialDestination) // Navigate to the home screen
                }
            })
        }

        composable(AskLoginDestination.routeName) {
            AskLogin(
                loginSuccess = {
                    navController.navigate(DriverDestination.routeName)
                },
                navigateUp = {
                    navController.popBackStack()
                },
                onLoginClick = {
                    navController.navigate(LoginDestination.routeName)
                },
                askForDetails = {
                    navController.navigate(UserDetailsDestination.routeName)
                }
            )
        }

        composable(LoginDestination.routeName) {
            LoginScreen(
                loginSuccess = { navController.navigate(DriverDestination.routeName) },
                onNavigateUp = { navController.navigateUp() },
                onRegisterClick = {
                    navController.navigate(RegisterDestination.routeName)
                },
                onForgetPassword = {
                    navController.navigate(ForgotPasswordDestination.routeName)
                }
            )
        }

        composable(ForgotPasswordDestination.routeName) {
            ForgotPasswordScreen(
                onNavigateUp = {
                    navController.popBackStack()
                },
                onEmailSent = {
                    navController.navigate(LoginDestination.routeName)
                }
            )
        }

        composable(UserDetailsDestination.routeName) {
            UserDetails(onSuccess = {
                navController.navigate(DriverDestination.routeName)
            })
        }

        composable(RegisterDestination.routeName) {
            RegisterScreen(registerSuccess = {
                navController.navigate(UserDetailsDestination.routeName)

            },
                onNavigateUp = {
                    navController.navigateUp()
                }
            )
        }


        // Driver screen destination
        composable(DriverDestination.routeName) {
            // Display the home screen composable
            DriverScreen(
                navController = navController,
                viewModel = driverViewModel,
                applicationViewModel = applicationViewModel
            )
        }

        // Driver screen destination
        composable(PassengerDestination.routeName) {
            // Display the home screen composable
            PassengerScreen(navController = navController,
                onClick = {
                    scope.launch {
                        firebase.signOut()
                        googleAuthUiClient.signOut()
                        navController.navigate(AskLoginDestination.routeName)
                    }
                }
            )
        }

        composable(ProfileDestination.routeName) {
            ProfileScreen(
                onLogout = {
                    scope.launch {
                        firebase.signOut()
                        googleAuthUiClient.signOut()
                        navController.navigate(AskLoginDestination.routeName)
                    }
                },
                onPastCarpool = {
                    navController.navigate(PastCarpoolsDestination.routeName)
                },
                editProfile = {
                    navController.navigate(EditProfileDestination.routeName)
                },
                onNavigateUp = {
                    navController.navigateUp()
                }
            )
        }

        composable(EditProfileDestination.routeName) {
            EditProfileScreen(onSuccess = {
                navController.navigateUp()
            })
        }

        composable(PastCarpoolsDestination.routeName) {
            PastCarpoolsScreen()
        }
    }
}
