package uk.ac.tees.mad.d3656925.ui.screens

import android.content.ContentValues
import android.content.Context
import android.graphics.Bitmap
import android.location.Location
import android.util.Log
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.EnterTransition
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.wrapContentSize
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Star
import androidx.compose.material.icons.filled.ToggleOff
import androidx.compose.material.icons.filled.ToggleOn
import androidx.compose.material.icons.outlined.PersonOutline
import androidx.compose.material3.BottomSheetScaffold
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.VerticalDivider
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
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawWithContent
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.PathEffect
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.content.ContextCompat
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavHostController
import coil.compose.AsyncImage
import coil.request.ImageRequest
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.LocationSource
import com.google.android.gms.maps.model.BitmapDescriptor
import com.google.android.gms.maps.model.BitmapDescriptorFactory
import com.google.android.gms.maps.model.CameraPosition
import com.google.android.gms.maps.model.LatLng
import com.google.firebase.Firebase
import com.google.firebase.auth.auth
import com.google.maps.android.compose.GoogleMap
import com.google.maps.android.compose.MapProperties
import com.google.maps.android.compose.MapUiSettings
import com.google.maps.android.compose.Marker
import com.google.maps.android.compose.rememberCameraPositionState
import com.google.maps.android.compose.rememberMarkerState
import kotlinx.coroutines.launch
import uk.ac.tees.mad.d3656925.R
import uk.ac.tees.mad.d3656925.domain.LoginStatus
import uk.ac.tees.mad.d3656925.domain.TripDetail
import uk.ac.tees.mad.d3656925.navigation.BottomNavigationScreens
import uk.ac.tees.mad.d3656925.navigation.CommuterBottomNavigation
import uk.ac.tees.mad.d3656925.navigation.NavigationDestination
import uk.ac.tees.mad.d3656925.navigation.bottomNavigationItems
import uk.ac.tees.mad.d3656925.ui.components.AddTripPopupBox
import uk.ac.tees.mad.d3656925.ui.components.CarpoolTopBar
import uk.ac.tees.mad.d3656925.ui.viewmodels.DriverViewModel
import uk.ac.tees.mad.d3656925.utils.location.ApplicationViewModel
import uk.ac.tees.mad.d3656925.utils.location.LocationRepository
import uk.ac.tees.mad.d3656925.utils.location.PreferencesManager
import java.text.DecimalFormat
import kotlin.random.Random

object DriverDestination : NavigationDestination {
    override val routeName: String
        get() = "driver"
    override val titleResource: Int
        get() = R.string.driver
}

private val locationSource = MyLocationSource()

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DriverScreen(
    navController: NavHostController,
    viewModel: DriverViewModel,
    applicationViewModel: ApplicationViewModel
) {
    val userDetailsState by viewModel.currentUserStatus.collectAsState(initial = null)
    val startTripState by viewModel.newTripStatus.collectAsState(initial = null)
    val markTripAsCompleteState by viewModel.markTripCompleteStatus.collectAsState(initial = null)
    val cancelTripStatus by viewModel.cancelTripStatus.collectAsState(initial = null)
    val currentTripDetail by viewModel.currentTrip.collectAsState()
    var showNewTripPopUp by remember {
        mutableStateOf(false)
    }
    val context = LocalContext.current
    val preferenceManager = PreferencesManager(context = context)
    val isDriverActive = preferenceManager.getIsActiveStatus()

    val activity = context as ComponentActivity
    val coroutineScope = rememberCoroutineScope()
    val locationRepository = LocationRepository(context, activity)
    val screenHeight =
        LocalContext.current.resources.displayMetrics.heightPixels.dp / LocalDensity.current.density
    val sheetPeekHeight = if (isDriverActive == true) screenHeight / 3 else 0.dp
    val sheetState = rememberStandardBottomSheetState(
        skipHiddenState = false
    )
    val bottomSheetScaffoldState = rememberBottomSheetScaffoldState(
        sheetState
    )

    LaunchedEffect(Unit) {
        viewModel.getUserDetails()
        viewModel.getCurrentTripForDriver()
    }

    // Adjust the bottom sheet visibility and height based on the driver's active status
    LaunchedEffect(isDriverActive) {
        coroutineScope.launch {
            if (isDriverActive == true) {
                viewModel.getCurrentTripForDriver()
                applicationViewModel.updateDriverLocation(Firebase.auth.currentUser?.uid!!)
                bottomSheetScaffoldState.bottomSheetState.expand()
            } else {
                bottomSheetScaffoldState.bottomSheetState.hide()
            }
        }
    }
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
    BottomSheetScaffold(
        scaffoldState = bottomSheetScaffoldState,
        sheetContent = {
            if (isDriverActive == true) {
                val currentTrip = currentTripDetail.data
                BottomSheetScaffoldContent(
                    currentTrip = currentTrip,
                    viewModel = viewModel,
                    markTripAsCompleteState = markTripAsCompleteState,
                    cancelTripStatus = cancelTripStatus
                )
            }
        },
        sheetPeekHeight = sheetPeekHeight
    ) {
        Scaffold(
            bottomBar = {
                CommuterBottomNavigation(
                    tabBarItems = bottomNavigationItems,
                    navController = navController,
                    selectedTabIndex = bottomNavigationItems.indexOf(BottomNavigationScreens.Driver),
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
            floatingActionButton = {
                FloatingActionButton(
                    onClick = {
                        if (isDriverActive == false) {
                            showNewTripPopUp = true
                        } else {
                            viewModel.updateDriverStatus()
                            viewModel.getUserDetails()
                        }
                    },
                    containerColor = if (isDriverActive == false) Color.Red else Color.Green
                ) {
//                if (userDetailsState?.isLoading == true) {
//                    CircularProgressIndicator()
//                } else {
                    if (isDriverActive == false) {
                        Icon(
                            imageVector = Icons.Default.ToggleOff,
                            contentDescription = "Set status On"
                        )
                    } else {
                        Icon(
                            imageVector = Icons.Default.ToggleOn,
                            contentDescription = "Set status off"
                        )
                    }
//                }
                }
            }
        ) {

            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(it)
            )
            {
                AddTripPopupBox(
                    showPopup = showNewTripPopUp,
                    onClickOutside = {
                        showNewTripPopUp = false
                    },
                    onConfirm = { startDestination: String, endDestination: String, price: String, time: Long, latitude: Double, longitude: Double ->
                        viewModel.startNewTrip(
                            startDestination,
                            endDestination,
                            time,
                            price.toDouble(),
                            latitude,
                            longitude
                        )
                    },
                    isLoading = startTripState?.isLoading == true,
                    latitude = locationState.value.latitude,
                    longitude = locationState.value.longitude
                )
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

                    val icon = bitmapDescriptor(
                        context, R.drawable.car_marker
                    )
                    Marker(
                        state = markerState,
                        title = "Driver: (${locationState.value.latitude}, ${
                            locationState.value.longitude
                        })",
                        visible = isDriverActive == true,
                        icon = icon,
                        onClick = { marker ->
                            marker.showInfoWindow()
                            coroutineScope.launch {
                                bottomSheetScaffoldState.bottomSheetState.show()
                            }
                            true
                        }
                    )
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


    LaunchedEffect(userDetailsState?.isError) {
        userDetailsState?.isError?.let {
            Toast.makeText(context, it, Toast.LENGTH_SHORT).show()
        }
    }
    LaunchedEffect(startTripState?.isSuccess) {
        startTripState?.isSuccess?.let {
            viewModel.getUserDetails()
            viewModel.getCurrentTripForDriver()
            showNewTripPopUp = false
            Toast.makeText(context, it, Toast.LENGTH_SHORT).show()
        }
    }
    LaunchedEffect(markTripAsCompleteState?.isSuccess) {
        markTripAsCompleteState?.isSuccess?.let {
            viewModel.updateDriverStatus()
            viewModel.getCurrentTripForDriver()
            viewModel.getUserDetails()
            bottomSheetScaffoldState.bottomSheetState.hide()
            Toast.makeText(context, it, Toast.LENGTH_SHORT).show()
        }
    }
    LaunchedEffect(cancelTripStatus?.isSuccess) {
        cancelTripStatus?.isSuccess?.let {
            bottomSheetScaffoldState.bottomSheetState.hide()
            viewModel.getCurrentTripForDriver()
            viewModel.getUserDetails()
            Toast.makeText(context, it, Toast.LENGTH_SHORT).show()
        }
    }
}

@Composable
fun BottomSheetScaffoldContent(
    currentTrip: TripDetail?,
    viewModel: DriverViewModel,
    markTripAsCompleteState: LoginStatus?,
    cancelTripStatus: LoginStatus?,
) {

    Column(
        Modifier
            .fillMaxWidth()
            .padding(16.dp)
    ) {

        Row(Modifier.fillMaxWidth()) {
            Text(
                text = "Ride Details",
                fontSize = 22.sp,
                fontWeight = FontWeight.Medium,
                textAlign = TextAlign.Center
            )
        }
        Row(verticalAlignment = Alignment.CenterVertically) {
            Box(
                modifier = Modifier
                    .padding(16.dp)
                    .size(70.dp)
            ) {
                Box(
                    modifier = Modifier
                        .clip(CircleShape)
                        .size(70.dp)
                ) {
                    if (currentTrip?.driverImage?.isEmpty() == true) {
                        Icon(
                            imageVector = Icons.Outlined.PersonOutline,
                            contentDescription = "Profile photo",
                            tint = Color.Gray
                        )
                    } else {
                        AsyncImage(
                            model = ImageRequest.Builder(LocalContext.current)
                                .crossfade(true)
                                .data(currentTrip?.driverImage).build(),
                            contentDescription = "Profile photo",
                            contentScale = ContentScale.Crop,
                            modifier = Modifier.size(70.dp)
                        )
                    }
                }
                Row(
                    modifier = Modifier
                        .align(Alignment.BottomCenter)
                        .clip(RoundedCornerShape(12.dp))
                        .background(MaterialTheme.colorScheme.primary)
                        .padding(2.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        imageVector = Icons.Default.Star,
                        contentDescription = null,
                        tint = Color.Yellow,
                        modifier = Modifier.size(16.dp)
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(
                        text = DecimalFormat("#.#")
                            .format(currentTrip?.driverRating ?: 4.5),
                        color = Color.White,
                        fontSize = 14.sp
                    )

                }
            }
            Column {
                Text(
                    text = buildAnnotatedString {

                        pushStyle(
                            SpanStyle(
                                fontSize = 20.sp,
                                fontWeight = FontWeight.Medium
                            )
                        )
                        append("Car name:   ")
                        pushStyle(
                            SpanStyle(
                                fontSize = 20.sp,
                                fontWeight = FontWeight.Medium
                            )
                        )
                        append("${currentTrip?.driverCarName}")
                    }
                )
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = buildAnnotatedString {

                        pushStyle(
                            SpanStyle(
                                fontSize = 16.sp,
                                fontWeight = FontWeight.Medium
                            )
                        )
                        append("Car number:   ")
                        pushStyle(
                            SpanStyle(
                                fontSize = 16.sp,
                                fontWeight = FontWeight.Medium
                            )
                        )
                        append("${currentTrip?.driverCarNumber}")
                    }
                )

            }
        }
        Row(
            Modifier
                .fillMaxWidth()
                .height(100.dp)
        ) {
            val pathEffect = PathEffect.dashPathEffect(floatArrayOf(10f, 10f), 0f)
            Column(
                modifier = Modifier
                    .fillMaxHeight(),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text(text = "Start", color = Color.Red)
                VerticalDivider(
                    modifier = Modifier
                        .weight(1f)
                        .drawWithContent {
                            drawLine(
                                color = Color.Red,
                                start = Offset(0f, 0f),
                                end = Offset(0f, size.height),
                                pathEffect = pathEffect
                            )
                        }
                )
                Text(text = "End", color = Color.Red)
            }
            Spacer(modifier = Modifier.width(20.dp))
            Column(
                Modifier.fillMaxHeight(),
                verticalArrangement = Arrangement.SpaceBetween
            ) {
                Row {
                    Text(
                        text = currentTrip?.startLocation ?: "Start",
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Medium
                    )
                }
                Row {

                    Text(
                        text = currentTrip?.endLocation ?: "End",
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Medium
                    )
                }
            }
        }
        Spacer(modifier = Modifier.height(16.dp))
        Column(
            Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(16.dp))
                .border(1.dp, Color.LightGray, RoundedCornerShape(16.dp))
        ) {
            Row(
                Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(text = "Available seats: ")
                Text(
                    text = "${currentTrip?.availableSeats ?: 5} seats available",
                    fontWeight = FontWeight.Medium
                )
            }
            HorizontalDivider()
            Row(
                Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(text = "Driver's phone: ")
                Text(
                    text = currentTrip?.driverPhone ?: "9856321470",
                    fontWeight = FontWeight.Medium
                )
            }
            HorizontalDivider()
            Row(
                Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(text = "Ride share: ")
                Text(
                    text = "€${currentTrip?.price ?: 5}",
                    fontWeight = FontWeight.Medium
                )
            }
        }
        Spacer(modifier = Modifier.height(16.dp))
        Row(Modifier.fillMaxWidth()) {
            Button(
                onClick = {
                    viewModel.markTripAsComplete(currentTrip?.tripId ?: "trip_id")
                },
                modifier = Modifier.weight(1f),
                colors = ButtonDefaults.buttonColors(containerColor = Color.Green)
            ) {
                if (markTripAsCompleteState?.isLoading == true) {
                    CircularProgressIndicator()
                } else {
                    Text(text = "Mark as complete")
                }
            }
            Spacer(modifier = Modifier.width(12.dp))
            OutlinedButton(
                onClick = {
                    viewModel.cancelTrip(currentTrip?.tripId ?: "trip_id")
                },
                border = BorderStroke(1.dp, Color.Red),

                modifier = Modifier.weight(1f)
            ) {
                if (cancelTripStatus?.isLoading == true) {
                    CircularProgressIndicator()
                } else {
                    Text(text = "Cancel", color = Color.Red)
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

fun bitmapDescriptor(
    context: Context,
    vectorResId: Int
): BitmapDescriptor? {

    // retrieve the actual drawable
    val drawable = ContextCompat.getDrawable(context, vectorResId) ?: return null
    drawable.setBounds(0, 0, drawable.intrinsicWidth, drawable.intrinsicHeight)
    val bm = Bitmap.createBitmap(
        drawable.intrinsicWidth,
        drawable.intrinsicHeight,
        Bitmap.Config.ARGB_8888
    )

    // draw it onto the bitmap
    val canvas = android.graphics.Canvas(bm)
    drawable.draw(canvas)
    return BitmapDescriptorFactory.fromBitmap(bm)
}