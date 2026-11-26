package com.example.realmatch.ui.chats

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.realmatch.data.api.toUserMessage
import com.example.realmatch.data.local.TokenManager
import com.example.realmatch.data.models.ChatListItem
import com.example.realmatch.data.models.ChatMessage
import com.example.realmatch.data.models.InvitePreview
import com.example.realmatch.data.models.Offer
import com.example.realmatch.data.models.PublicUserProfile
import com.example.realmatch.data.repository.ChatsRepository
import com.example.realmatch.data.repository.InviteRepository
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

data class ChatsUiState(
    val isLoading: Boolean = true,
    val chats: List<ChatListItem> = emptyList(),
    val error: String? = null,
    val activeChat: ChatListItem? = null,
    val messages: List<ChatMessage> = emptyList(),
    val messagesLoading: Boolean = false,
    val messagesError: String? = null,
    val isSending: Boolean = false,
    val profileUserId: String? = null,
    val profile: PublicUserProfile? = null,
    val isProfileLoading: Boolean = false,
    val profileError: String? = null,
    val myUserId: String? = null,
    val offers: List<Offer> = emptyList(),
    val offersLoading: Boolean = false,
    val offersError: String? = null,
    val showOffers: Boolean = false,
    val invitingOfferId: String? = null,
    val inviteToken: String? = null,
    val invitePreview: InvitePreview? = null,
    val inviteLoading: Boolean = false,
    val inviteError: String? = null,
    val acceptingInvite: Boolean = false,
    val inviteAccepted: Boolean = false,
)

class ChatsViewModel(application: Application) : AndroidViewModel(application) {

    private val repository = ChatsRepository(application)
    private val inviteRepository = InviteRepository(application)
    private val myUserId: String? = TokenManager.getInstance(application).accountId
    private var refreshingMessages = false

    private val _state = MutableStateFlow(ChatsUiState(myUserId = myUserId))
    val state: StateFlow<ChatsUiState> = _state.asStateFlow()

    fun loadChats() {
        viewModelScope.launch {
            _state.update { it.copy(isLoading = true, error = null) }
            try {
                val chats = repository.getChats()
                _state.update { it.copy(isLoading = false, chats = chats) }
            } catch (e: CancellationException) {
                throw e
            } catch (e: Exception) {
                _state.update { it.copy(isLoading = false, error = e.toUserMessage()) }
            }
        }
    }

    fun openChat(chat: ChatListItem) {
        _state.update {
            it.copy(
                activeChat = chat,
                messages = emptyList(),
                messagesError = null,
                messagesLoading = true,
                showOffers = false,
                offersError = null,
                inviteToken = null,
                invitePreview = null,
                inviteError = null,
                inviteAccepted = false,
                profileUserId = null,
                profile = null,
            )
        }
        viewModelScope.launch {
            loadMessages(chat)
            repository.markRead(chat)
            loadChats()
        }
    }

    fun closeChat() {
        _state.update {
            it.copy(
                activeChat = null,
                messages = emptyList(),
                messagesError = null,
                showOffers = false,
                inviteToken = null,
                invitePreview = null,
                inviteError = null,
                inviteAccepted = false,
            )
        }
        loadChats()
    }

    /** Вызывается по таймеру, пока экран чата открыт. */
    fun refreshMessages() {
        val chat = _state.value.activeChat ?: return
        if (refreshingMessages) return
        viewModelScope.launch { loadMessages(chat, silent = true) }
    }

    fun sendMessage(text: String) {
        val chat = _state.value.activeChat ?: return
        val cleaned = text.trim()
        if (cleaned.isEmpty() || _state.value.isSending) return
        viewModelScope.launch {
            _state.update { it.copy(isSending = true, messagesError = null) }
            try {
                repository.sendMessage(chat, cleaned)
                _state.update { it.copy(isSending = false) }
                loadMessages(chat)
            } catch (e: CancellationException) {
                throw e
            } catch (e: Exception) {
                _state.update { it.copy(isSending = false, messagesError = e.toUserMessage()) }
            }
        }
    }

    fun openProfile(userId: String) {
        _state.update {
            it.copy(profileUserId = userId, profile = null, profileError = null, isProfileLoading = true)
        }
        viewModelScope.launch {
            try {
                val profile = repository.getUser(userId)
                _state.update { it.copy(isProfileLoading = false, profile = profile) }
            } catch (e: CancellationException) {
                throw e
            } catch (e: Exception) {
                _state.update { it.copy(isProfileLoading = false, profileError = e.toUserMessage()) }
            }
        }
    }

    fun closeProfile() {
        _state.update { it.copy(profileUserId = null, profile = null, profileError = null) }
    }

    fun retryProfile() {
        _state.value.profileUserId?.let(::openProfile)
    }

    fun openOffers() {
        _state.update { it.copy(showOffers = true, offersError = null) }
        if (_state.value.offers.isEmpty()) loadOffers()
    }

    fun closeOffers() {
        _state.update { it.copy(showOffers = false, offersError = null) }
    }

    fun retryOffers() {
        loadOffers()
    }

    private fun loadOffers() {
        viewModelScope.launch {
            _state.update { it.copy(offersLoading = true, offersError = null) }
            try {
                val offers = inviteRepository.getOffers()
                _state.update { it.copy(offersLoading = false, offers = offers) }
            } catch (e: CancellationException) {
                throw e
            } catch (e: Exception) {
                _state.update { it.copy(offersLoading = false, offersError = e.toUserMessage()) }
            }
        }
    }

    fun createInvite(offerId: String) {
        val chat = _state.value.activeChat ?: return
        if (_state.value.invitingOfferId != null) return
        viewModelScope.launch {
            _state.update { it.copy(invitingOfferId = offerId, offersError = null) }
            try {
                inviteRepository.createInvite(
                    offerId = offerId,
                    initiatorId = chat.initiatorId,
                    responderId = chat.responderId,
                )
                _state.update { it.copy(invitingOfferId = null, showOffers = false) }
                loadMessages(chat, silent = true)
            } catch (e: CancellationException) {
                throw e
            } catch (e: Exception) {
                _state.update { it.copy(invitingOfferId = null, offersError = e.toUserMessage()) }
            }
        }
    }

    fun openInvite(token: String) {
        _state.update {
            it.copy(
                inviteToken = token,
                invitePreview = null,
                inviteLoading = true,
                inviteError = null,
                inviteAccepted = false,
            )
        }
        viewModelScope.launch {
            try {
                val preview = inviteRepository.getPreview(token)
                _state.update { it.copy(inviteLoading = false, invitePreview = preview) }
            } catch (e: CancellationException) {
                throw e
            } catch (e: Exception) {
                _state.update { it.copy(inviteLoading = false, inviteError = e.toUserMessage()) }
            }
        }
    }

    fun retryInvite() {
        _state.value.inviteToken?.let(::openInvite)
    }

    fun closeInvite() {
        _state.update {
            it.copy(
                inviteToken = null,
                invitePreview = null,
                inviteError = null,
                inviteAccepted = false,
            )
        }
    }

    fun acceptInvite() {
        val token = _state.value.inviteToken ?: return
        if (_state.value.acceptingInvite) return
        viewModelScope.launch {
            _state.update { it.copy(acceptingInvite = true, inviteError = null) }
            try {
                inviteRepository.accept(token)
                _state.update { it.copy(acceptingInvite = false, inviteAccepted = true) }
                _state.value.activeChat?.let { loadMessages(it, silent = true) }
                loadChats()
            } catch (e: CancellationException) {
                throw e
            } catch (e: Exception) {
                _state.update { it.copy(acceptingInvite = false, inviteError = e.toUserMessage()) }
            }
        }
    }

    private suspend fun loadMessages(chat: ChatListItem, silent: Boolean = false) {
        if (refreshingMessages) return
        refreshingMessages = true
        if (!silent) {
            _state.update { it.copy(messagesLoading = true) }
        }
        try {
            val messages = repository.getMessages(chat)
            _state.update {
                it.copy(
                    messages = messages,
                    messagesLoading = false,
                    messagesError = null,
                )
            }
            if (messages.any { !it.isRead && it.authorId != myUserId }) {
                repository.markRead(chat)
            }
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            _state.update {
                it.copy(
                    messagesLoading = false,
                    messagesError = if (silent) it.messagesError else e.toUserMessage(),
                )
            }
        } finally {
            refreshingMessages = false
        }
    }
}
