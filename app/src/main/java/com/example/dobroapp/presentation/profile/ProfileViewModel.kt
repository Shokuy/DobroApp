package com.example.dobroapp.presentation.profile

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.dobroapp.domain.model.ProfileSummary
import com.example.dobroapp.domain.model.UserRole
import com.example.dobroapp.domain.repository.ProfileRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class ProfileViewModel(
    private val profileRepository: ProfileRepository
) : ViewModel() {
    private val _profile = MutableStateFlow<ProfileSummary?>(null)
    val profile: StateFlow<ProfileSummary?> = _profile.asStateFlow()

    fun load(role: UserRole, userId: String) {
        if (userId.isBlank()) return
        viewModelScope.launch {
            runCatching {
                _profile.value = profileRepository.getProfile(role, userId)
            }.onFailure {
                _profile.value = null
            }
        }
    }
}
