package com.example.realmatch.ui.partner

import android.app.Application
import android.net.Uri
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.realmatch.data.api.toUserMessage
import com.example.realmatch.data.models.PartnerOffer
import com.example.realmatch.data.models.PartnerProfile
import com.example.realmatch.data.repository.PartnerRepository
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

data class PartnerUiState(
    val isLoading: Boolean = true,
    val profile: PartnerProfile? = null,
    val offers: List<PartnerOffer> = emptyList(),
    val error: String? = null,
    val isSaving: Boolean = false,
    val saveSuccess: Boolean = false,
    val actionError: String? = null,
    val deactivatingId: String? = null,
)

class PartnerCabinetViewModel(application: Application) : AndroidViewModel(application) {

    private val repository = PartnerRepository(application)

    private val _state = MutableStateFlow(PartnerUiState())
    val state: StateFlow<PartnerUiState> = _state.asStateFlow()

    fun load() {
        viewModelScope.launch {
            _state.update { it.copy(isLoading = true, error = null) }
            try {
                val profile = repository.getMe()
                val offers = repository.getOffers()
                _state.update { it.copy(isLoading = false, profile = profile, offers = offers) }
            } catch (e: CancellationException) {
                throw e
            } catch (e: Exception) {
                _state.update { it.copy(isLoading = false, error = e.toUserMessage()) }
            }
        }
    }

    fun saveOffer(
        offerId: String?,
        title: String,
        discountInfo: String,
        validUntilIso: String,
        photoUri: Uri?,
    ) {
        if (_state.value.isSaving) return
        viewModelScope.launch {
            _state.update { it.copy(isSaving = true, actionError = null) }
            try {
                if (offerId == null) {
                    requireNotNull(photoUri) { "Не выбрано фото" }
                    repository.createOffer(title, discountInfo, validUntilIso, photoUri)
                } else {
                    repository.updateOffer(offerId, title, discountInfo, validUntilIso, photoUri)
                }
                val offers = repository.getOffers()
                _state.update { it.copy(isSaving = false, saveSuccess = true, offers = offers) }
            } catch (e: CancellationException) {
                throw e
            } catch (e: Exception) {
                _state.update { it.copy(isSaving = false, actionError = e.toUserMessage()) }
            }
        }
    }

    fun deactivateOffer(offerId: String) {
        if (_state.value.deactivatingId != null) return
        viewModelScope.launch {
            _state.update { it.copy(deactivatingId = offerId, actionError = null) }
            try {
                repository.deactivateOffer(offerId)
                val offers = repository.getOffers()
                _state.update { it.copy(deactivatingId = null, offers = offers) }
            } catch (e: CancellationException) {
                throw e
            } catch (e: Exception) {
                _state.update { it.copy(deactivatingId = null, actionError = e.toUserMessage()) }
            }
        }
    }

    fun consumeSaveSuccess() {
        _state.update { it.copy(saveSuccess = false) }
    }

    fun clearActionError() {
        _state.update { it.copy(actionError = null) }
    }
}
