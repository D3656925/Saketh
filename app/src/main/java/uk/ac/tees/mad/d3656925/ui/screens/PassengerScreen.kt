package uk.ac.tees.mad.d3656925.ui.screens

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.navigation.NavHostController
import uk.ac.tees.mad.d3656925.R
import uk.ac.tees.mad.d3656925.navigation.BottomNavigationScreens
import uk.ac.tees.mad.d3656925.navigation.CommuterBottomNavigation
import uk.ac.tees.mad.d3656925.navigation.NavigationDestination
import uk.ac.tees.mad.d3656925.navigation.bottomNavigationItems


object PassengerDestination : NavigationDestination {
    override val routeName: String
        get() = "passenger"
    override val titleResource: Int
        get() = R.string.passenger
}


@Composable
fun PassengerScreen(navController: NavHostController, onClick: () -> Unit) {

    Scaffold(
        bottomBar = {
            CommuterBottomNavigation(
                tabBarItems = bottomNavigationItems,
                navController = navController,
                selectedTabIndex = bottomNavigationItems.indexOf(BottomNavigationScreens.Passenger),
            )
        }
    ) {

        Column(
            modifier = Modifier
                .padding(it)
                .fillMaxSize(),
            verticalArrangement = Arrangement.Center,
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(text = "Passenger", modifier = Modifier.clickable{onClick()})
        }
    }
}