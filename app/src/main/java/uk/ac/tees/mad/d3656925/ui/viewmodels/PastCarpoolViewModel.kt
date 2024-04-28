package uk.ac.tees.mad.d3656925.ui.viewmodels

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.launch
import uk.ac.tees.mad.d3656925.data.DriverRepository
import uk.ac.tees.mad.d3656925.domain.Resource
import uk.ac.tees.mad.d3656925.domain.TripDetail
import javax.inject.Inject

@HiltViewModel
class PastCarpoolViewModel @Inject constructor(
    private val repository: DriverRepository
) : ViewModel() {

    private val _pastCarpoolStatus = Channel<ResponseState>()
    val pastCarpoolStatus = _pastCarpoolStatus.receiveAsFlow()

    init {
        getPastCarpools()
    }

    fun getPastCarpools() = viewModelScope.launch {
        repository.getAllPastTrips().collect {
            when (it) {
                is Resource.Error -> {
                    _pastCarpoolStatus.send(ResponseState(isError = it.message))
                }

                is Resource.Loading -> {
                    _pastCarpoolStatus.send(ResponseState(isLoading = true))
                }

                is Resource.Success -> {
                    _pastCarpoolStatus.send(ResponseState(isSuccess = it.data))
                }
            }
        }
    }
}

data class ResponseState(
    val isLoading: Boolean = false,
    val isSuccess: List<TripDetail>? = null,
    val isError: String? = null
)