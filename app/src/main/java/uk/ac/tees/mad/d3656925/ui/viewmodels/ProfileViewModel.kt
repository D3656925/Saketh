package uk.ac.tees.mad.d3656925.ui.viewmodels

import android.content.Context
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.launch
import uk.ac.tees.mad.d3656925.data.CommuterCarpoolingRepository
import uk.ac.tees.mad.d3656925.data.DriverRepository
import uk.ac.tees.mad.d3656925.domain.Resource
import uk.ac.tees.mad.d3656925.ui.auth.CurrentUser
import uk.ac.tees.mad.d3656925.utils.location.PreferencesManager
import javax.inject.Inject

@HiltViewModel
class ProfileViewModel @Inject constructor(
    private val repository: DriverRepository,
    private val userRepository: CommuterCarpoolingRepository
) : ViewModel() {

    private val _currentUserStatus = Channel<CurrentUser>()
    val currentUserStatus = _currentUserStatus.receiveAsFlow()

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
}