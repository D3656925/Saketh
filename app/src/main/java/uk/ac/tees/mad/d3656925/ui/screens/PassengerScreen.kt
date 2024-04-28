package uk.ac.tees.mad.d3656925.ui.screens

import android.content.ContentValues
import android.util.Log
import androidx.activity.ComponentActivity
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.EnterTransition
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.wrapContentSize
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.rememberBottomSheetScaffoldState
import androidx.compose.material3.rememberStandardBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.navigation.NavHostController
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.model.CameraPosition
import com.google.android.gms.maps.model.LatLng
import com.google.maps.android.compose.GoogleMap
import com.google.maps.android.compose.MapProperties
import com.google.maps.android.compose.MapUiSettings
import com.google.maps.android.compose.Marker
import com.google.maps.android.compose.rememberCameraPositionState
import com.google.maps.android.compose.rememberMarkerState
import kotlinx.coroutines.launch
import uk.ac.tees.mad.d3656925.R
import uk.ac.tees.mad.d3656925.navigation.BottomNavigationScreens
import uk.ac.tees.mad.d3656925.navigation.CommuterBottomNavigation
import uk.ac.tees.mad.d3656925.navigation.NavigationDestination
import uk.ac.tees.mad.d3656925.navigation.bottomNavigationItems
import uk.ac.tees.mad.d3656925.ui.components.CarpoolTopBar
import uk.ac.tees.mad.d3656925.ui.viewmodels.DriverViewModel
import uk.ac.tees.mad.d3656925.ui.viewmodels.PassengerViewModel
import uk.ac.tees.mad.d3656925.utils.location.ApplicationViewModel
import uk.ac.tees.mad.d3656925.utils.location.LocationRepository


object PassengerDestination : NavigationDestination {
    override val routeName: String
        get() = "passenger"
    override val titleResource: Int
        get() = R.string.passenger
}


@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PassengerScreen(
    navController: NavHostController,
    onTripClick: (String) -> Unit,
    applicationViewModel: ApplicationViewModel,
    viewModel: PassengerViewModel,
    driverViewModel: DriverViewModel,
    onSignOut: () -> Unit
) {
    val allTripListStatus = viewModel.allTripsStatus.collectAsState(initial = null)
    val userDetailsState by driverViewModel.currentUserStatus.collectAsState(initial = null)

    val context = LocalContext.current

    val activity = context as ComponentActivity
    val coroutineScope = rememberCoroutineScope()
    val locationRepository = LocationRepository(context, activity)
    val sheetState = rememberStandardBottomSheetState(
        skipHiddenState = false
    )
    val bottomSheetScaffoldState = rememberBottomSheetScaffoldState(
        sheetState
    )

    val uiSettings = remember {
        MapUiSettings(zoomControlsEnabled = false)
    }

    var zoom by remember {
        mutableFloatStateOf(8f)
    }
    var isMapLoaded by remember { mutableStateOf(false) }

    // To show blue dot on map
    val mapProperties by remember { mutableStateOf(MapProperties(isMyLocationEnabled = true)) }

    // Collect location updates
    val locationState =
        applicationViewModel.locationFlow.collectAsState(initial = newLocation())
    val markerState = rememberMarkerState(
        position = LatLng(
            locationState.value.latitude,
            locationState.value.longitude
        )
    )
    val defaultLocation =
        LatLng(locationState.value.latitude, locationState.value.longitude)
    val defaultCameraPosition = CameraPosition.fromLatLngZoom(defaultLocation, 1f)

    // To control and observe the map camera
    val cameraPositionState = rememberCameraPositionState {
        position = defaultCameraPosition
    }

    val isGpsEnabled = locationRepository.gpsStatus.collectAsState(initial = false)

    // Update blue dot and camera when the location changes
    LaunchedEffect(locationState.value) {
        Log.d(ContentValues.TAG, "Updating blue dot on map...")
        locationSource.onLocationChanged(locationState.value)
        if (!isGpsEnabled.value) {
            locationRepository.checkGpsSettings()
        } else {
            Log.d(ContentValues.TAG, "Updating camera position...")
            markerState.position = LatLng(
                locationState.value.latitude,
                locationState.value.longitude
            )
            cameraPositionState.animate(
                CameraUpdateFactory.newLatLngZoom(
                    LatLng(
                        locationState.value.latitude,
                        locationState.value.longitude
                    ), zoom
                ),
                1000
            )
        }
    }

    // Detect when the map starts moving and print the reason
    LaunchedEffect(cameraPositionState.isMoving) {
        if (cameraPositionState.isMoving) {
            Log.d(
                ContentValues.TAG,
                "Map camera started moving due to ${cameraPositionState.position}"
            )
        }
    }
    Scaffold(
        bottomBar = {
            CommuterBottomNavigation(
                tabBarItems = bottomNavigationItems,
                navController = navController,
                selectedTabIndex = bottomNavigationItems.indexOf(BottomNavigationScreens.Passenger),
            )
        },
        topBar = {
            CarpoolTopBar(
                userDetailsState = userDetailsState,
                onProfileClick = {
                    navController.navigate(ProfileDestination.routeName)
                }
            )
        },
    ) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(it)
        )
        {
            GoogleMap(
                modifier = Modifier.matchParentSize(),
                uiSettings = uiSettings,
                onMapLongClick = { latlng ->
//                    viewModel.onEvent(MapEvent.OnMapLongClick(latlng))
                },
                cameraPositionState = cameraPositionState,
                onMapLoaded = {
                    isMapLoaded = true
                },
                // This listener overrides the behavior for the location button. It is intended to be used when a
                // custom behavior is needed.
                onMyLocationButtonClick = {
                    Log.d(
                        ContentValues.TAG,
                        "Overriding the onMyLocationButtonClick with this Log"
                    );
                    if (!isGpsEnabled.value) {
                        locationRepository.checkGpsSettings()
                    } else {
                        coroutineScope.launch {
                            cameraPositionState.animate(
                                CameraUpdateFactory.newLatLngZoom(
                                    LatLng(
                                        locationState.value.latitude,
                                        locationState.value.longitude
                                    ), zoom
                                ),
                                1000
                            )
                        }
                    }
                    true
                },
                locationSource = locationSource,
                properties = mapProperties
            ) {
                val userIcon = bitmapDescriptor(
                    context, R.drawable.user
                )
                val carIcon = bitmapDescriptor(
                    context, R.drawable.car_icon
                )
                Marker(
                    state = markerState,
                    title = "Me: (${locationState.value.latitude}, ${
                        locationState.value.longitude
                    })",
                    icon = userIcon,
                    onClick = { marker ->
                        marker.showInfoWindow()
                        coroutineScope.launch {
                            bottomSheetScaffoldState.bottomSheetState.show()
                        }
                        true
                    }
                )
                allTripListStatus.value?.isSuccess?.forEach { trip ->
                    val state = rememberMarkerState(
                        position = LatLng(trip.coordinate.latitude, trip.coordinate.longitude)
                    )
                    Marker(
                        state = state,
                        title = "Passenger: ${trip.coordinate}",
                        icon = carIcon,
                        onClick = { marker ->
                            onTripClick(trip.tripId)
                            true
                        }
                    )
                }
            }
            if (!isMapLoaded) {
                AnimatedVisibility(
                    modifier = Modifier
                        .matchParentSize(),
                    visible = !isMapLoaded,
                    enter = EnterTransition.None,
                    exit = fadeOut()
                ) {
                    CircularProgressIndicator(
                        modifier = Modifier
                            .background(MaterialTheme.colorScheme.background)
                            .wrapContentSize()
                    )
                }
            }
        }
    }
}