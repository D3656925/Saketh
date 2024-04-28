package uk.ac.tees.mad.d3656925.ui.viewmodels

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.GeoPoint
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.launch
import uk.ac.tees.mad.d3656925.data.CommuterCarpoolingRepository
import uk.ac.tees.mad.d3656925.data.DriverRepository
import uk.ac.tees.mad.d3656925.domain.LoginStatus
import uk.ac.tees.mad.d3656925.domain.Resource
import uk.ac.tees.mad.d3656925.domain.TripDetail
import uk.ac.tees.mad.d3656925.ui.auth.CurrentUser
import javax.inject.Inject

@HiltViewModel
class DriverViewModel @Inject constructor(
    private val repository: DriverRepository,
    private val userRepository: CommuterCarpoolingRepository
) : ViewModel() {


    private val _currentUserStatus = Channel<CurrentUser>()
    val currentUserStatus = _currentUserStatus.receiveAsFlow()

    private val _updateDriverStatus = Channel<LoginStatus>()
    val updateDriverStatus = _updateDriverStatus.receiveAsFlow()

    private val _markTripCompleteStatus = Channel<LoginStatus>()
    val markTripCompleteStatus = _markTripCompleteStatus.receiveAsFlow()

    private val _cancelTripStatus = Channel<LoginStatus>()
    val cancelTripStatus = _cancelTripStatus.receiveAsFlow()

    private val _newTripStatus = Channel<LoginStatus>()
    val newTripStatus = _newTripStatus.receiveAsFlow()

    init {
        getUserDetails()
    }

    fun getUserDetails() =
        viewModelScope.launch {
            userRepository.getCurrentUser().collect { result ->
                when (result) {
                    is Resource.Error -> {
                        _currentUserStatus.send(CurrentUser(isError = result.message))
                    }

                    is Resource.Loading -> {
                        _currentUserStatus.send(CurrentUser(isLoading = true))
                    }

                    is Resource.Success -> {
                        _currentUserStatus.send(CurrentUser(isSuccess = result.data))

                    }
                }
            }
        }

    private val _currentTrip = MutableStateFlow<Resource<TripDetail>>(Resource.Loading())
    val currentTrip = _currentTrip.asStateFlow()

    fun getCurrentTripForDriver() {
        viewModelScope.launch {
            repository.getCurrentTripForDriver().collect { result ->
                _currentTrip.value = result
            }
        }
    }

    fun updateDriverStatus() =
        viewModelScope.launch {
            userRepository.updateDriverStatus().collect { result ->
                when (result) {
                    is Resource.Error -> {
                        _updateDriverStatus.send(LoginStatus(isError = result.message))
                    }

                    is Resource.Loading -> {
                        _updateDriverStatus.send(LoginStatus(isLoading = true))
                    }

                    is Resource.Success -> {
                        _updateDriverStatus.send(LoginStatus(isSuccess = result.data))

                    }
                }
            }
        }

    fun markTripAsComplete(tripId: String) =
        viewModelScope.launch {
            repository.markTripAsCompleted(tripId = tripId).collect { result ->
                when (result) {
                    is Resource.Error -> {
                        _markTripCompleteStatus.send(LoginStatus(isError = result.message))
                    }

                    is Resource.Loading -> {
                        _markTripCompleteStatus.send(LoginStatus(isLoading = true))
                    }

                    is Resource.Success -> {
                        _markTripCompleteStatus.send(LoginStatus(isSuccess = result.data))
                    }
                }
            }
        }

    fun cancelTrip(tripId: String) =
        viewModelScope.launch {
            repository.cancelTrip(tripId = tripId).collect { result ->
                when (result) {
                    is Resource.Error -> {
                        _cancelTripStatus.send(LoginStatus(isError = result.message))
                    }

                    is Resource.Loading -> {
                        _cancelTripStatus.send(LoginStatus(isLoading = true))
                    }

                    is Resource.Success -> {
                        _cancelTripStatus.send(LoginStatus(isSuccess = result.data))
                    }
                }
            }
        }


    fun startNewTrip(
        startLocation: String,
        endLocation: String,
        startTime: Long,
        price: Double,
        latitude: Double,
        longitude: Double
    ) = viewModelScope.launch {
        repository.addNewTrip(
            startLocation,
            endLocation,
            startTime,
            price,
            GeoPoint(latitude, longitude)
        )
            .collect { result ->
                when (result) {
                    is Resource.Error -> {
                        _newTripStatus.send(LoginStatus(isError = result.message))
                    }

                    is Resource.Loading -> {
                        _newTripStatus.send(LoginStatus(isLoading = true))
                    }

                    is Resource.Success -> {
                        _newTripStatus.send(LoginStatus(isSuccess = result.data))
                    }
                }
            }
    }


}

data class DriverUiState(
    val name: String = "",
    val imageUrl: String = "",
    val email: String = "",
    val phone: String = "",
    val location: String = ""
)