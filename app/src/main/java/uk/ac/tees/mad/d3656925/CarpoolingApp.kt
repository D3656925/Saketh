package uk.ac.tees.mad.d3656925

import androidx.compose.runtime.Composable
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import uk.ac.tees.mad.d3656925.ui.screens.HomeScreen
import uk.ac.tees.mad.d3656925.ui.screens.HomeScreenDestination
import uk.ac.tees.mad.d3656925.ui.screens.SplashDestination
import uk.ac.tees.mad.d3656925.ui.screens.SplashScreen

@Composable
fun CarpoolingApp() {
    // Create a NavHostController to manage navigation within the app
    val navController = rememberNavController()

    // Define the navigation graph using NavHost
    NavHost(navController = navController, startDestination = SplashDestination.routeName) {
        // Splash screen destination
        composable(SplashDestination.routeName) {
            // Display the splash screen composable
            SplashScreen(navController = navController)
        }
        // Home screen destination
        composable(HomeScreenDestination.routeName) {
            // Display the home screen composable
            HomeScreen(navController = navController)
        }
    }
}
