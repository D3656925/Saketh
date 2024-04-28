package uk.ac.tees.mad.d3656925.utils.location

import android.Manifest
import android.annotation.SuppressLint
import android.app.Application
import android.content.pm.PackageManager
import android.location.Location
import android.os.Looper
import android.util.Log
import androidx.compose.runtime.mutableStateOf
import androidx.core.app.ActivityCompat
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.google.android.gms.location.LocationCallback
import com.google.android.gms.location.LocationRequest
import com.google.android.gms.location.LocationResult
import com.google.android.gms.location.LocationServices
import com.google.android.gms.location.Priority
import com.google.firebase.Firebase
import com.google.firebase.auth.auth
import com.google.firebase.firestore.GeoPoint
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.flow.shareIn
import kotlinx.coroutines.launch
import uk.ac.tees.mad.d3656925.data.DriverRepository
import javax.inject.Inject

@HiltViewModel
class ApplicationViewModel @Inject constructor(
    application: Application,
    private val repository: DriverRepository
) :
    AndroidViewModel(application) {

    private val fusedLocationProviderClient =
        LocationServices.getFusedLocationProviderClient(application)

    private var locationState = mutableStateOf(uk.ac.tees.mad.d3656925.domain.Location())

    private var locationCallback: LocationCallback

    @SuppressLint("MissingPermission")
    val locationFlow = callbackFlow {
        val locationCallback = object : LocationCallback() {
            override fun onLocationResult(locationResult: LocationResult) {
                super.onLocationResult(locationResult)
                for (location in locationResult.locations) {
                    trySend(location)
                }
            }
        }


        // Request location updates
        fusedLocationProviderClient.requestLocationUpdates(
            locationRequest,
            locationCallback,
            Looper.getMainLooper()
        )

        // Await close and remove the location updates when the flow is no longer collected
        awaitClose {
            fusedLocationProviderClient.removeLocationUpdates(locationCallback)
        }
    }.shareIn(
        viewModelScope,
        replay = 0,
        started = SharingStarted.WhileSubscribed()
    )

    init {
        locationCallback = object : LocationCallback() {
            override fun onLocationResult(locationResult: LocationResult) {
                super.onLocationResult(locationResult)
                for (location in locationResult.locations) {
                    setLocationData(location)

                }
            }
        }
        startLocationUpdate()
    }

    fun updateDriverLocation(driverId: String) {
        val locationCallback = object : LocationCallback() {
            override fun onLocationResult(locationResult: LocationResult) {
                for (location in locationResult.locations) {
                    // Convert android.location.Location to Firestore GeoPoint
                    val geoPoint = GeoPoint(location.latitude, location.longitude)
                    // Update the driver's location in Firestore
                    viewModelScope.launch {
                        repository.updateDriverLocation(Firebase.auth.currentUser?.uid!!, geoPoint)
                            .collect { resource ->
                                Log.d("DRIVER location update", resource.message.toString())
                            }
                    }
                }
            }
        }


        if (ActivityCompat.checkSelfPermission(
                getApplication(),
                Manifest.permission.ACCESS_FINE_LOCATION
            ) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(
                getApplication(),
                Manifest.permission.ACCESS_COARSE_LOCATION
            ) != PackageManager.PERMISSION_GRANTED
        ) {
            // TODO: Consider calling
            //    ActivityCompat#requestPermissions
            // here to request the missing permissions, and then overriding
            //   public void onRequestPermissionsResult(int requestCode, String[] permissions,
            //                                          int[] grantResults)
            // to handle the case where the user grants the permission. See the documentation
            // for ActivityCompat#requestPermissions for more details.
            return
        }
        fusedLocationProviderClient.requestLocationUpdates(locationRequest, locationCallback, Looper.getMainLooper())
    }

    fun startLocationUpdate() {
        if (ActivityCompat.checkSelfPermission(
                getApplication(),
                Manifest.permission.ACCESS_FINE_LOCATION
            ) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(
                getApplication(),
                Manifest.permission.ACCESS_COARSE_LOCATION
            ) != PackageManager.PERMISSION_GRANTED
        ) {
            // TODO: Consider calling
            //    ActivityCompat#requestPermissions
            // here to request the missing permissions, and then overriding
            //   public void onRequestPermissionsResult(int requestCode, String[] permissions,
            //                                          int[] grantResults)
            // to handle the case where the user grants the permission. See the documentation
            // for ActivityCompat#requestPermissions for more details.
            return
        }
        fusedLocationProviderClient.requestLocationUpdates(
            locationRequest,
            locationCallback,
            Looper.getMainLooper()
        )
    }

    private fun setLocationData(location: Location?) {
        location?.let { newLocation ->
            locationState.value = uk.ac.tees.mad.d3656925.domain.Location(
                latitude = newLocation.latitude,
                longitude = newLocation.longitude
            )
        }
    }

    companion object {
        private const val ONE_MINUTE: Long = 60_000L
        val locationRequest = LocationRequest.Builder(Priority.PRIORITY_HIGH_ACCURACY, ONE_MINUTE)
            .setWaitForAccurateLocation(false)
            .setMinUpdateIntervalMillis(ONE_MINUTE / 4)
            .setMaxUpdateDelayMillis(ONE_MINUTE / 2)
            .build()

    }
}