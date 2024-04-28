package uk.ac.tees.mad.d3656925.ui.screens

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.IntrinsicSize
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ChevronLeft
import androidx.compose.material.icons.filled.DriveEta
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.ToggleOff
import androidx.compose.material.icons.filled.ToggleOn
import androidx.compose.material.icons.outlined.PersonOutline
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import coil.compose.AsyncImage
import coil.request.ImageRequest
import uk.ac.tees.mad.d3656925.R
import uk.ac.tees.mad.d3656925.navigation.NavigationDestination
import uk.ac.tees.mad.d3656925.ui.viewmodels.ProfileViewModel
import uk.ac.tees.mad.d3656925.utils.location.PreferencesManager

object ProfileDestination : NavigationDestination {
    override val routeName: String
        get() = "profile"
    override val titleResource: Int
        get() = R.string.profile
}


@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ProfileScreen(
    onLogout: () -> Unit,
    onPastCarpool: () -> Unit,
    editProfile: () -> Unit,
    onNavigateUp: () -> Unit
) {
    val viewModel: ProfileViewModel = hiltViewModel()
    val userDetailsState by viewModel.currentUserStatus.collectAsState(initial = null)
    val user = userDetailsState?.isSuccess?.item
    val context = LocalContext.current
    val preferencesManager = PreferencesManager(context)
    LaunchedEffect(Unit) {
        viewModel.getUserDetails()
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(text = "Profile") },
                navigationIcon = {
                    IconButton(onClick = onNavigateUp) {
                        Icon(
                            imageVector = Icons.Default.ChevronLeft,
                            contentDescription = "BAck"
                        )
                    }
                },
                actions = {
                    Button(onClick = onLogout) {
                        Text(text = "Logout")
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    titleContentColor = MaterialTheme.colorScheme.primary,
                    actionIconContentColor = MaterialTheme.colorScheme.primary
                )
            )
        },
    ) { innerPadding ->
        Column(
            Modifier
                .fillMaxSize()
                .padding(innerPadding)
        ) {
            Row(
                Modifier
                    .height(IntrinsicSize.Max)
                    .padding(16.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Card(
                    modifier = Modifier
                        .size(80.dp)
                        .padding(16.dp),
                    shape = CircleShape
                ) {
                    if (user?.profileImage?.isEmpty() == true) {
                        Icon(
                            imageVector = Icons.Outlined.PersonOutline,
                            contentDescription = "Profile photo"
                        )
                    } else {
                        AsyncImage(
                            model = ImageRequest.Builder(LocalContext.current)
                                .crossfade(true)
                                .data(userDetailsState?.isSuccess?.item?.profileImage)
                                .build(),
                            contentDescription = "Profile photo",
                            contentScale = ContentScale.FillBounds
                        )
                    }
                }

                Column() {
                    Text(
                        text = user?.name ?: "Guest",
                        fontWeight = FontWeight.Medium,
                        fontSize = 20.sp
                    )
                    Text(
                        text = user?.email ?: "Guest",
                        fontWeight = FontWeight.Medium,
                        fontSize = 16.sp
                    )
                }
            }

            Column(
                Modifier
                    .fillMaxWidth()
                    .padding(24.dp),
                verticalArrangement = Arrangement.spacedBy(20.dp)
            ) {
                Row(
                    Modifier
                        .fillMaxWidth()
                        .clickable { editProfile() },
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(imageVector = Icons.Default.Edit, contentDescription = null)
                    Spacer(modifier = Modifier.width(16.dp))
                    Text(text = "Edit profile", fontSize = 18.sp)
                }
                HorizontalDivider()
                Row(
                    Modifier
                        .fillMaxWidth()
                        .clickable { onPastCarpool() },
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(imageVector = Icons.Default.DriveEta, contentDescription = null)
                    Spacer(modifier = Modifier.width(16.dp))
                    Text(text = "Carpool history", fontSize = 18.sp)
                }
                HorizontalDivider()

                if (preferencesManager.getIsActiveStatus()) {
                    Row(
                        Modifier
                            .fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(imageVector = Icons.Default.ToggleOn, contentDescription = null)
                        Spacer(modifier = Modifier.width(16.dp))
                        Text(text = "Active", fontSize = 18.sp)
                    }
                } else {
                    Row(
                        Modifier
                            .fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(imageVector = Icons.Default.ToggleOff, contentDescription = null)
                        Spacer(modifier = Modifier.width(16.dp))
                        Text(text = "Inactive", fontSize = 18.sp)
                    }
                }
                HorizontalDivider()
            }
        }
    }
}