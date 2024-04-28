package uk.ac.tees.mad.d3656925.ui.screens

import androidx.compose.foundation.layout.Column
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import uk.ac.tees.mad.d3656925.R
import uk.ac.tees.mad.d3656925.navigation.NavigationDestination


object PastCarpoolsDestination : NavigationDestination {
    override val routeName: String
        get() = "past_carpools"
    override val titleResource: Int
        get() = R.string.profile
}

@Composable
fun PastCarpoolsScreen() {
    Column {
        Text(text = "Past carpools")
    }
}