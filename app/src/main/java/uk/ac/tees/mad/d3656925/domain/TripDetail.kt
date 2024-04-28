package uk.ac.tees.mad.d3656925.domain

import com.google.firebase.firestore.GeoPoint

data class TripDetail(
    val tripId: String,
    val driverId: String,
    val driverPhone: String,
    val driverCarNumber: String,
    val driverRating: Double,
    val driverCarName: String,
    val driverName: String,
    val driverImage: String,
    val startLocation: String,
    val endLocation: String,
    val startTime: Long,
    val price: Double,
    val availableSeats: Int,
    val isActive: Boolean = true,
    val isCancelled: Boolean = false,
    val coordinate: GeoPoint = GeoPoint(0.0, 0.0)
)