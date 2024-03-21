package uk.ac.tees.mad.d3656925.ui.screens

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.size
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavHostController
import com.airbnb.lottie.compose.LottieAnimation
import com.airbnb.lottie.compose.LottieCompositionSpec
import com.airbnb.lottie.compose.rememberLottieComposition
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import uk.ac.tees.mad.d3656925.R
import uk.ac.tees.mad.d3656925.navigation.NavigationDestination

object SplashDestination : NavigationDestination {
    override val routeName: String
        get() = "splash" // Route name for the splash destination
    override val titleResource: Int
        get() = R.string.app_name // Title resource for the splash screen
}

@Composable
fun SplashScreen(navController: NavHostController) {
    // Animation duration using Animatable
    val animTime = remember {
        Animatable(0f)
    }

    // Retrieve Lottie composition
    val animationFile = R.raw.carpooling_anim
    val composition by rememberLottieComposition(spec = LottieCompositionSpec.RawRes(animationFile))

    // Launch effect for animation and navigation
    LaunchedEffect(key1 = true) {
        animTime.animateTo(1f, animationSpec = tween(1500)) // Animation to scale the splash screen elements
        delay(3000L) // Delay before navigating to the next screen
        launch(Dispatchers.Main) {
            navController.popBackStack() // Clear any existing back stack
            navController.navigate(HomeScreenDestination.routeName) // Navigate to the home screen
        }
    }

    // Column layout for the splash screen content
    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.White), // Background color for the splash screen
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        // Lottie animation component
        LottieAnimation(
            composition = composition,
            modifier = Modifier.size(300.dp) // Size of the animation
        )
        Spacer(modifier = Modifier.height(30.dp)) // Spacer for layout
        // App name text
        Text(
            text = stringResource(id = R.string.app_name),
            fontSize = 32.sp, // Font size
            fontWeight = FontWeight.Bold, // Font weight
            color = MaterialTheme.colorScheme.primary, // Text color
            modifier = Modifier.alpha(animTime.value).scale(animTime.value) // Alpha and scale animation for text
        )
        Spacer(modifier = Modifier.height(5.dp)) // Spacer for layout
        // Subtitle text
        Text(
            text = "Share the ride and joy",
            fontSize = 18.sp, // Font size
            fontWeight = FontWeight.Bold, // Font weight
            color = MaterialTheme.colorScheme.primary, // Text color
            modifier = Modifier.alpha(animTime.value) // Alpha animation for text
        )
    }
}
