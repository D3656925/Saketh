package uk.ac.tees.mad.d3656925.ui.viewmodels

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.google.firebase.Firebase
import com.google.firebase.auth.auth
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.launch
import uk.ac.tees.mad.d3656925.data.DriverRepository
import uk.ac.tees.mad.d3656925.domain.LoginStatus
import uk.ac.tees.mad.d3656925.domain.Resource
import uk.ac.tees.mad.d3656925.domain.TripDetail
import uk.ac.tees.mad.d3656925.ui.screens.TripDetailDestination
import javax.inject.Inject

@HiltViewModel
class TripDetailViewModel @Inject constructor(
    private val repository: DriverRepository,
    savedStateHandle: SavedStateHandle
) : ViewModel() {

    private val tripId: String = checkNotNull(savedStateHandle[TripDetailDestination.tripIdArg])

    private val _getTripStatus = Channel<ResponseStatus>()
    val getTripStatus = _getTripStatus.receiveAsFlow()


    private val _joinTripStatus = Channel<LoginStatus>()
    val joinTripStatus = _joinTripStatus.receiveAsFlow()

    private val _hasUserJoined = Channel<Boolean>()
    val hasUserJoined = _hasUserJoined.receiveAsFlow()

    init {
        getTrip()
        hasUserJoined()
    }

    fun getTrip() = viewModelScope.launch {
        repository.getTripDetails(tripId).collect {
            when (it) {
                is Resource.Error -> {
                    _getTripStatus.send(ResponseStatus(isError = it.message))
                }

                is Resource.Loading -> {
                    _getTripStatus.send(ResponseStatus(isLoading = true))
                }

                is Resource.Success -> {
                    _getTripStatus.send(ResponseStatus(isSuccess = it.data))
                }
            }
        }
    }

    fun joinTrip() = viewModelScope.launch {
        repository.joinTrip(tripId, Firebase.auth.currentUser?.uid!!).collect {
            when (it) {
                is Resource.Error -> {
                    _joinTripStatus.send(LoginStatus(isError = it.message))
                }

                is Resource.Loading -> {
                    _joinTripStatus.send(LoginStatus(isLoading = true))
                }

                is Resource.Success -> {
                    _joinTripStatus.send(LoginStatus(isSuccess = it.data))
                }
            }
        }
    }

    fun hasUserJoined() = viewModelScope.launch {
        repository.hasUserJoinedTrip(tripId, Firebase.auth.currentUser?.uid!!).collect {
            when (it) {
                is Resource.Error -> {
                    _hasUserJoined.send(false)
                }

                is Resource.Loading -> {
                    _hasUserJoined.send(false)
                }

                is Resource.Success -> {
                    _hasUserJoined.send(true)
                }
            }
        }
    }

}

data class ResponseStatus(
    val isLoading: Boolean = false,
    val isSuccess: TripDetail? = null,
    val isError: String? = null
)
