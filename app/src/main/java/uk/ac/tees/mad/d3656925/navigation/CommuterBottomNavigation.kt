package uk.ac.tees.mad.d3656925.navigation

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController


val bottomNavigationItems = listOf(
    BottomNavigationScreens.Driver,
    BottomNavigationScreens.Passenger,
)

@Composable
fun CommuterBottomNavigation(
    tabBarItems: List<BottomNavigationScreens>,
    navController: NavController,
    selectedTabIndex: Int
) {
    val interactionSource = remember {
        MutableInteractionSource()
    }
    Row(
        modifier = Modifier
            .clip(RoundedCornerShape(20.dp))
            .padding(horizontal = 18.dp)
            .fillMaxWidth()
            .background(Color.White),
        horizontalArrangement = Arrangement.Center
    ) {
        Row(
            modifier = Modifier
                .clip(RoundedCornerShape(20.dp))
                .fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceAround
        ) {
            tabBarItems.forEachIndexed { index, screen ->
                NavItem(
                    selected = selectedTabIndex == index,
                    onClick = {
                        navController.navigate(tabBarItems[index].route)
                    },
                    tabBarItem = tabBarItems[index],
                    interactionSource = interactionSource
                )
            }
        }
    }
}

@Composable
fun NavItem(
    selected: Boolean,
    onClick: () -> Unit,
    tabBarItem: BottomNavigationScreens,
    interactionSource: MutableInteractionSource
) {

    Column(
        Modifier
            .clickable(interactionSource = interactionSource, indication = null) {
                onClick()
            }
            .height(70.dp)

    ) {
        Column(
            Modifier
                .weight(1f),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {

            Icon(
                imageVector = tabBarItem.selectedIcon,
                contentDescription = tabBarItem.route,
                tint = if (selected) MaterialTheme.colorScheme.primary else Color.Black,
                modifier = Modifier.size(25.dp)
            )

            Text(
                text = stringResource(id = tabBarItem.nameRes),
                fontSize = 14.sp,
                color = if (selected) MaterialTheme.colorScheme.primary else Color.Black
            )
        }
    }
}