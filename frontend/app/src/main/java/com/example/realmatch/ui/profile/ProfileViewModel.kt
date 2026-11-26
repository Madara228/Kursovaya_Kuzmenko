package com.example.realmatch.ui.profile

import android.app.Application
import android.net.Uri
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.realmatch.data.api.toUserMessage
import com.example.realmatch.data.models.UserProfile
import com.example.realmatch.data.repository.UserRepository
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

data class ProfileUiState(
    val isLoading: Boolean = true,
    val profile: UserProfile? = null,
    val error: String? = null,
    val isEditing: Boolean = false,
    val editCity: String = "",
    val editBio: String = "",
    val isSaving: Boolean = false,
    val isPhotoEditing: Boolean = false,
    val isPhotoBusy: Boolean = false,
)

class ProfileViewModel(application: Application) : AndroidViewModel(application) {

    private val repository = UserRepository(application)

    private val _state = MutableStateFlow(ProfileUiState())
    val state: StateFlow<ProfileUiState> = _state.asStateFlow()

    init {
        loadProfile()
    }

    fun loadProfile() {
        viewModelScope.launch {
            _state.update { it.copy(isLoading = true, error = null) }
            try {
                val profile = repository.getMe()
                _state.update { it.copy(isLoading = false, profile = profile) }
            } catch (e: CancellationException) {
                throw e
            } catch (e: Exception) {
                _state.update { it.copy(isLoading = false, error = e.toUserMessage()) }
            }
        }
    }

    fun startEditing() {
        _state.update { state ->
            val profile = state.profile ?: return@update state
            state.copy(
                isEditing = true,
                editCity = profile.city.orEmpty(),
                editBio = profile.bio.orEmpty(),
                error = null,
            )
        }
    }

    fun cancelEditing() {
        _state.update { it.copy(isEditing = false, error = null) }
    }

    fun setCity(value: String) {
        _state.update { it.copy(editCity = value) }
    }

    fun setBio(value: String) {
        _state.update { it.copy(editBio = value) }
    }

    fun saveProfile() {
        if (_state.value.isSaving) return
        viewModelScope.launch {
            _state.update { it.copy(isSaving = true, error = null) }
            try {
                val profile = repository.updateProfile(
                    city = _state.value.editCity.trim(),
                    bio = _state.value.editBio.trim(),
                )
                _state.update { it.copy(isSaving = false, isEditing = false, profile = profile) }
            } catch (e: CancellationException) {
                throw e
            } catch (e: Exception) {
                _state.update { it.copy(isSaving = false, error = e.toUserMessage()) }
            }
        }
    }

    fun togglePhotoEditing() {
        _state.update { it.copy(isPhotoEditing = !it.isPhotoEditing, error = null) }
    }

    fun uploadPhoto(uri: Uri?) {
        if (uri == null || _state.value.isPhotoBusy) return
        viewModelScope.launch {
            _state.update { it.copy(isPhotoBusy = true, error = null) }
            try {
                repository.uploadPhoto(uri)
                val profile = repository.getMe()
                _state.update { it.copy(isPhotoBusy = false, profile = profile) }
            } catch (e: CancellationException) {
                throw e
            } catch (e: Exception) {
                _state.update { it.copy(isPhotoBusy = false, error = e.toUserMessage()) }
            }
        }
    }

    fun setMainPhoto(photoId: String) {
        if (_state.value.isPhotoBusy) return
        viewModelScope.launch {
            _state.update { it.copy(isPhotoBusy = true, error = null) }
            try {
                repository.setMainPhoto(photoId)
                val profile = repository.getMe()
                _state.update { it.copy(isPhotoBusy = false, profile = profile) }
            } catch (e: CancellationException) {
                throw e
            } catch (e: Exception) {
                _state.update { it.copy(isPhotoBusy = false, error = e.toUserMessage()) }
            }
        }
    }

    fun deletePhoto(photoId: String) {
        if (_state.value.isPhotoBusy) return
        viewModelScope.launch {
            _state.update { it.copy(isPhotoBusy = true, error = null) }
            try {
                repository.deletePhoto(photoId)
                val profile = repository.getMe()
                _state.update { it.copy(isPhotoBusy = false, profile = profile) }
            } catch (e: CancellationException) {
                throw e
            } catch (e: Exception) {
                _state.update { it.copy(isPhotoBusy = false, error = e.toUserMessage()) }
            }
        }
    }
}
