package com.example.realmatch.ui.auth

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.realmatch.data.api.toUserMessage
import com.example.realmatch.data.local.UserRole
import com.example.realmatch.data.repository.AuthRepository
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

data class AuthUiState(
    val isLoading: Boolean = false,
    val error: String? = null,
    val loggedInRole: UserRole? = null,
)

class AuthViewModel(application: Application) : AndroidViewModel(application) {

    private val repository = AuthRepository(application)

    private val _state = MutableStateFlow(AuthUiState())
    val state: StateFlow<AuthUiState> = _state.asStateFlow()

    fun login(login: String, password: String, role: UserRole) {
        if (_state.value.isLoading) return
        if (login.isBlank() || password.isBlank()) {
            _state.update { it.copy(error = "Введите логин и пароль") }
            return
        }
        viewModelScope.launch {
            _state.update { it.copy(isLoading = true, error = null) }
            try {
                if (role == UserRole.USER) {
                    repository.loginAsUser(login.trim(), password)
                } else {
                    repository.loginAsPartner(login.trim(), password)
                }
                _state.update { it.copy(isLoading = false, loggedInRole = role) }
            } catch (e: CancellationException) {
                throw e
            } catch (e: Exception) {
                _state.update { it.copy(isLoading = false, error = e.toUserMessage()) }
            }
        }
    }
}
