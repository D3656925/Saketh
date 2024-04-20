package uk.ac.tees.mad.d3656925.navigation

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.DriveEta
import androidx.compose.material.icons.outlined.PersonOutline
import androidx.compose.ui.graphics.vector.ImageVector
import uk.ac.tees.mad.d3656925.ui.screens.DriverDestination
import uk.ac.tees.mad.d3656925.ui.screens.PassengerDestination

sealed class BottomNavigationScreens(
    val route: String,
    val selectedIcon: ImageVector,
    val nameRes: Int
) {
    object Passenger : BottomNavigationScreens(
        route = PassengerDestination.routeName,
        selectedIcon = Icons.Outlined.PersonOutline,
        nameRes = PassengerDestination.titleResource
    )

    object Driver : BottomNavigationScreens(
        route = DriverDestination.routeName,
        selectedIcon = Icons.Outlined.DriveEta,
        nameRes = DriverDestination.titleResource
    )
}