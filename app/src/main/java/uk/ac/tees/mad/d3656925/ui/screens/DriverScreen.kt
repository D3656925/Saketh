package uk.ac.tees.mad.d3656925.ui.screens

import android.content.ContentValues
import android.location.Location
import android.util.Log
import android.widget.Toast
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
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
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
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavHostController
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.LocationSource
import com.google.android.gms.maps.model.CameraPosition
import com.google.android.gms.maps.model.LatLng
import com.google.maps.android.compose.GoogleMap
import com.google.maps.android.compose.MapProperties
import com.google.maps.android.compose.MapUiSettings
import com.google.maps.android.compose.rememberCameraPositionState
import kotlinx.coroutines.launch
import uk.ac.tees.mad.d3656925.R
import uk.ac.tees.mad.d3656925.navigation.BottomNavigationScreens
import uk.ac.tees.mad.d3656925.navigation.CommuterBottomNavigation
import uk.ac.tees.mad.d3656925.navigation.NavigationDestination
import uk.ac.tees.mad.d3656925.navigation.bottomNavigationItems
import uk.ac.tees.mad.d3656925.utils.location.ApplicationViewModel
import uk.ac.tees.mad.d3656925.utils.location.LocationRepository
import kotlin.random.Random

object DriverDestination : NavigationDestination {
    override val routeName: String
        get() = "driver"
    override val titleResource: Int
        get() = R.string.driver
}

private val locationSource = MyLocationSource()

@Composable
fun DriverScreen(navController: NavHostController, onLogOut: () -> Unit) {
    val context = LocalContext.current
    val activity = context as ComponentActivity
    val coroutineScope = rememberCoroutineScope()
    val locationRepository = LocationRepository(context, activity)

    val uiSettings = remember {
        MapUiSettings(zoomControlsEnabled = false)
    }

    var zoom by remember {
        mutableFloatStateOf(8f)
    }
    var isMapLoaded by remember { mutableStateOf(false) }
    val applicationViewModel: ApplicationViewModel = hiltViewModel()

    // To show blue dot on map
    val mapProperties by remember { mutableStateOf(MapProperties(isMyLocationEnabled = true)) }

    // Collect location updates
    val locationState =
        applicationViewModel.locationFlow.collectAsState(initial = newLocation())

    val defaultLocation =
        LatLng(locationState.value.latitude, locationState.value.longitude)
    val defaultCameraPosition = CameraPosition.fromLatLngZoom(defaultLocation, 1f)

    // To control and observe the map camera
    val cameraPositionState = rememberCameraPositionState {
        position = defaultCameraPosition
    }

    val isGpsEnabled = locationRepository.gpsStatus.collectAsState(initial = false)

    val cameraPosition by remember {
        mutableStateOf(
            CameraPosition.fromLatLngZoom(
                LatLng(
                    locationState.value.latitude,
                    locationState.value.longitude
                ), zoom
            )
        )
    }
    // Update blue dot and camera when the location changes
    LaunchedEffect(locationState.value) {
        Log.d(ContentValues.TAG, "Updating blue dot on map...")
        locationSource.onLocationChanged(locationState.value)
        if (!isGpsEnabled.value) {
            locationRepository.checkGpsSettings()
        } else {
            Log.d(ContentValues.TAG, "Updating camera position...")
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
                selectedTabIndex = bottomNavigationItems.indexOf(BottomNavigationScreens.Driver),
            )
        }
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
                            cameraPositionState
                                .animate(
                                    update = CameraUpdateFactory.newCameraPosition(
                                        cameraPosition
                                    ),
                                    1500
                                )
                        }
                    }
                    true
                },
                locationSource = locationSource,
                properties = mapProperties
            ) {
//                viewModel.state.parkingSpots.forEach { spot ->
//                    Marker(
//                        position = LatLng(spot.lat, spot.lng),
//                        title = "Parking Spot (${spot.lat}, ${spot.lng})",
//                        snippet = "Long Click to delete",
//                        onInfoWindowLongClick = {
//                            viewModel.onEvent(MapEvent.OnInfoWindowLongClick(spot))
//                        },
//                        onClick = {
//                            it.showInfoWindow()
//                            true
//                        },
//                        icon = BitmapDescriptorFactory.defaultMarker(
//                            BitmapDescriptorFactory.HUE_GREEN
//                        )
//                    )
//                }
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

class MyLocationSource : LocationSource {

    private var listener: LocationSource.OnLocationChangedListener? = null

    override fun activate(listener: LocationSource.OnLocationChangedListener) {
        this.listener = listener
    }

    override fun deactivate() {
        listener = null
    }

    fun onLocationChanged(location: Location) {
        listener?.onLocationChanged(location)
    }
}

private fun newLocation(): Location {
    val location = Location("MyLocationProvider")
    location.apply {
        latitude = 51.5074 + Random.nextFloat()
        longitude = -0.1278 + Random.nextFloat()
    }
    return location
}