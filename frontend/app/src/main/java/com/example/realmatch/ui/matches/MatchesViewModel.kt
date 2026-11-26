package com.example.realmatch.ui.matches

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.realmatch.data.api.toUserMessage
import com.example.realmatch.data.models.MatchItem
import com.example.realmatch.data.models.PublicUserProfile
import com.example.realmatch.data.repository.MatchesRepository
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

data class MatchesUiState(
    val isLoading: Boolean = true,
    val matches: List<MatchItem> = emptyList(),
    val error: String? = null,
    val selectedUserId: String? = null,
    val detail: PublicUserProfile? = null,
    val isDetailLoading: Boolean = false,
    val detailError: String? = null,
)

class MatchesViewModel(application: Application) : AndroidViewModel(application) {

    private val repository = MatchesRepository(application)

    private val _state = MutableStateFlow(MatchesUiState())
    val state: StateFlow<MatchesUiState> = _state.asStateFlow()

    fun load() {
        viewModelScope.launch {
            _state.update { it.copy(isLoading = true, error = null) }
            try {
                val matches = repository.getMatches()
                _state.update { it.copy(isLoading = false, matches = matches) }
            } catch (e: CancellationException) {
                throw e
            } catch (e: Exception) {
                _state.update { it.copy(isLoading = false, error = e.toUserMessage()) }
            }
        }
    }

    fun openUser(userId: String) {
        _state.update {
            it.copy(selectedUserId = userId, detail = null, detailError = null, isDetailLoading = true)
        }
        viewModelScope.launch {
            try {
                val detail = repository.getUser(userId)
                _state.update { it.copy(isDetailLoading = false, detail = detail) }
            } catch (e: CancellationException) {
                throw e
            } catch (e: Exception) {
                _state.update { it.copy(isDetailLoading = false, detailError = e.toUserMessage()) }
            }
        }
    }

    fun closeUser() {
        _state.update { it.copy(selectedUserId = null, detail = null, detailError = null) }
    }

    fun retryDetail() {
        _state.value.selectedUserId?.let(::openUser)
    }
}
