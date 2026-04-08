package com.example.dobroapp.presentation.requests

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.dobroapp.domain.model.HelpRequest
import com.example.dobroapp.domain.model.HelpType
import com.example.dobroapp.domain.model.RequestStatus
import com.example.dobroapp.domain.model.UserRole
import com.example.dobroapp.domain.repository.RequestRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class RequestsViewModel(
    private val requestRepository: RequestRepository
) : ViewModel() {
    private val _myRequests = MutableStateFlow(emptyList<HelpRequest>())
    val myRequests: StateFlow<List<HelpRequest>> = _myRequests.asStateFlow()

    private val _openRequests = MutableStateFlow(emptyList<HelpRequest>())
    val openRequests: StateFlow<List<HelpRequest>> = _openRequests.asStateFlow()

    private val _acceptedByMe = MutableStateFlow(emptyList<HelpRequest>())
    val acceptedByMe: StateFlow<List<HelpRequest>> = _acceptedByMe.asStateFlow()

    private val _selectedDistrict = MutableStateFlow<String?>(null)
    val selectedDistrict: StateFlow<String?> = _selectedDistrict.asStateFlow()

    private var currentUserRole: UserRole = UserRole.Pensioner
    private var currentUserId: String = ""
    private var currentUserName: String = ""

    fun bindSession(role: UserRole, userId: String, fullName: String) {
        currentUserRole = role
        currentUserId = userId
        currentUserName = fullName
        refresh()
    }

    fun refresh() {
        if (currentUserId.isBlank()) return
        viewModelScope.launch {
            runCatching {
                _myRequests.value = requestRepository.getMyRequests(currentUserRole, currentUserId)
                _openRequests.value = requestRepository.getOpenRequests(_selectedDistrict.value)
                _acceptedByMe.value = _myRequests.value.filter {
                    it.volunteerId == currentUserId &&
                        (it.status == RequestStatus.Accepted || it.status == RequestStatus.InProgress)
                }
            }.onFailure {
                _myRequests.value = emptyList()
                _openRequests.value = emptyList()
                _acceptedByMe.value = emptyList()
            }
        }
    }

    fun setDistrictFilter(district: String?) {
        _selectedDistrict.value = district
        viewModelScope.launch {
            runCatching {
                _openRequests.value = requestRepository.getOpenRequests(district)
            }.onFailure {
                _openRequests.value = emptyList()
            }
        }
    }

    fun createRequest(
        title: String,
        rewardCoins: Int,
        district: String,
        address: String,
        time: String,
        comment: String,
        helpType: HelpType
    ) {
        viewModelScope.launch {
            runCatching {
                requestRepository.createRequest(
                    HelpRequest(
                        id = "",
                        title = title,
                        helpType = helpType,
                        rewardCoins = rewardCoins.coerceAtLeast(1),
                        district = district,
                        address = address,
                        time = time,
                        comment = comment,
                        pensionerName = currentUserName.ifBlank { "Пенсионер" },
                        pensionerId = currentUserId,
                        status = RequestStatus.Open
                    )
                )
                refresh()
            }
        }
    }

    fun acceptRequest(requestId: String) {
        viewModelScope.launch {
            runCatching {
                requestRepository.acceptRequest(requestId, currentUserId)
                refresh()
            }
        }
    }

    fun startRequest(requestId: String) {
        viewModelScope.launch {
            runCatching {
                requestRepository.startRequest(requestId, currentUserId)
                refresh()
            }
        }
    }

    fun completeRequest(requestId: String, rating: Int) {
        viewModelScope.launch {
            runCatching {
                requestRepository.completeRequest(requestId, rating)
                refresh()
            }
        }
    }
}
