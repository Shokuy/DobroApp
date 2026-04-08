package com.example.dobroapp.presentation.auth

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.dobroapp.domain.model.UserRole
import com.example.dobroapp.domain.model.UserSession
import com.example.dobroapp.domain.repository.AuthRepository
import kotlinx.coroutines.launch
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

class AuthViewModel(
    private val authRepository: AuthRepository
) : ViewModel() {
    private val _role = MutableStateFlow<UserRole?>(null)
    val role: StateFlow<UserRole?> = _role.asStateFlow()

    private val _session = MutableStateFlow<UserSession?>(null)
    val session: StateFlow<UserSession?> = _session.asStateFlow()

    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading.asStateFlow()

    private val _errorMessage = MutableStateFlow<String?>(null)
    val errorMessage: StateFlow<String?> = _errorMessage.asStateFlow()

    fun signIn(role: UserRole, fullName: String) {
        if (fullName.isBlank()) {
            _errorMessage.value = "Введите имя перед входом."
            return
        }
        viewModelScope.launch {
            _isLoading.value = true
            _errorMessage.value = null
            runCatching { authRepository.signIn(role, fullName.trim()) }
                .onSuccess {
                    _role.value = role
                    _session.value = it
                }
                .onFailure {
                    _errorMessage.value = "Нет подключения к серверу. Проверьте backend и попробуйте снова."
                }
            _isLoading.value = false
        }
    }

    fun signOut() {
        _role.value = null
        _session.value = null
        _errorMessage.value = null
        _isLoading.value = false
    }
}
