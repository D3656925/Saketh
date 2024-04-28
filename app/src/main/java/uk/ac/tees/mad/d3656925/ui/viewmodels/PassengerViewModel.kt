package uk.ac.tees.mad.d3656925.ui.viewmodels

import androidx.compose.runtime.mutableStateOf
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.launch
import uk.ac.tees.mad.d3656925.data.CommuterCarpoolingRepository
import uk.ac.tees.mad.d3656925.data.DriverRepository
import uk.ac.tees.mad.d3656925.domain.Resource
import uk.ac.tees.mad.d3656925.domain.TripDetail
import javax.inject.Inject

@HiltViewModel
class PassengerViewModel @Inject constructor(
    private val repository: DriverRepository,
    private val userRepository: CommuterCarpoolingRepository
) : ViewModel() {

    private val _allTripsStatus = Channel<ResponseState>()
    val allTripsStatus = _allTripsStatus.receiveAsFlow()


    var allTripsList = mutableStateOf<List<TripDetail>>(emptyList())

    init {
        getAllTrips()
    }

    fun getAllTrips() = viewModelScope.launch {
        repository.getAllTripsForPassengers().collect {
            when (it) {
                is Resource.Error -> {
                    _allTripsStatus.send(ResponseState(isError = it.message))
                }

                is Resource.Loading -> {
                    _allTripsStatus.send(ResponseState(isLoading = true))
                }

                is Resource.Success -> {
                    _allTripsStatus.send(ResponseState(isSuccess = it.data))
                    if (it.data != null) {
                        allTripsList.value = it.data
                    }
                }
            }
        }
    }
}
