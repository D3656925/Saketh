package uk.ac.tees.mad.d3656925.ui.viewmodels

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.google.firebase.Firebase
import com.google.firebase.auth.auth
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.launch
import uk.ac.tees.mad.d3656925.data.CommuterCarpoolingRepository
import uk.ac.tees.mad.d3656925.domain.LoginStatus
import uk.ac.tees.mad.d3656925.domain.Resource
import uk.ac.tees.mad.d3656925.ui.auth.CurrentUser
import javax.inject.Inject

@HiltViewModel
class EditViewModel @Inject constructor(
    private val repository: CommuterCarpoolingRepository
) : ViewModel() {
    private val _currentUserStatus = Channel<CurrentUser>()
    val currentUserStatus = _currentUserStatus.receiveAsFlow()

    private val _updateDetailStatus = Channel<LoginStatus>()
    val updateDetailsStatus = _updateDetailStatus.receiveAsFlow()


    init {
        getUserDetails()
    }

    fun getUserDetails() =
        viewModelScope.launch {

            repository.getCurrentUser().collect { result ->
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


    suspend fun updateUserDetail(
        userName: String,
        profileImage: ByteArray?
    ) {
        val user = Firebase.auth.currentUser
        repository.saveUser(user?.email, userName, user?.uid!!, profileImage)
    }
}