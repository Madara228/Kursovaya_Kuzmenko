package com.example.realmatch.ui.feed

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.realmatch.data.api.toUserMessage
import com.example.realmatch.data.models.FeedUser
import com.example.realmatch.data.repository.FeedRepository
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import retrofit2.HttpException

const val ACTION_LIKE = "LIKE"
const val ACTION_DISLIKE = "DISLIKE"

data class FeedUiState(
    val isLoading: Boolean = true,
    val users: List<FeedUser> = emptyList(),
    val index: Int = 0,
    val error: String? = null,
    val isSwiping: Boolean = false,
    val matchUser: FeedUser? = null,
    val myPhotoUrl: String? = null,
) {
    val current: FeedUser? get() = users.getOrNull(index)
}

class FeedViewModel(application: Application) : AndroidViewModel(application) {

    private val repository = FeedRepository(application)

    private val _state = MutableStateFlow(FeedUiState())
    val state: StateFlow<FeedUiState> = _state.asStateFlow()

    fun load() {
        if (_state.value.isLoading && _state.value.users.isNotEmpty()) return
        viewModelScope.launch {
            _state.update { it.copy(isLoading = true, error = null, matchUser = null) }
            try {
                val users = repository.getFeed()
                _state.update { it.copy(isLoading = false, users = users, index = 0) }
            } catch (e: CancellationException) {
                throw e
            } catch (e: Exception) {
                _state.update { it.copy(isLoading = false, error = e.toUserMessage()) }
            }
        }
        viewModelScope.launch {
            runCatching { repository.getMyMainPhoto() }
                .onSuccess { url -> _state.update { it.copy(myPhotoUrl = url) } }
        }
    }

    fun like() = swipe(ACTION_LIKE)

    fun dislike() = swipe(ACTION_DISLIKE)

    private fun swipe(action: String) {
        val current = _state.value.current ?: return
        if (_state.value.isSwiping) return
        // карточку убираем сразу, при ошибке вернём на место
        _state.update { it.copy(isSwiping = true, index = it.index + 1, error = null) }
        viewModelScope.launch {
            try {
                val result = repository.swipe(current.id, action)
                _state.update {
                    it.copy(
                        isSwiping = false,
                        matchUser = if (result.isMatch) current else it.matchUser,
                    )
                }
            } catch (e: CancellationException) {
                throw e
            } catch (e: Exception) {
                // 409 — контакт уже есть (например, анкета устарела): просто пропускаем
                val alreadySwiped = e is HttpException && e.code() == 409
                _state.update {
                    if (alreadySwiped) {
                        it.copy(isSwiping = false)
                    } else {
                        it.copy(isSwiping = false, index = it.index - 1, error = e.toUserMessage())
                    }
                }
            }
        }
    }

    fun dismissMatch() {
        _state.update { it.copy(matchUser = null) }
    }
}
