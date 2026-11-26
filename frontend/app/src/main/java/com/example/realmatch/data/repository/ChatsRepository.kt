package com.example.realmatch.data.repository

import android.content.Context
import com.example.realmatch.data.api.ApiClient
import com.example.realmatch.data.api.ChatsApi
import com.example.realmatch.data.api.UserApi
import com.example.realmatch.data.models.ChatListItem
import com.example.realmatch.data.models.ChatMessage
import com.example.realmatch.data.models.PublicUserProfile
import com.example.realmatch.data.models.SendMessageRequest

class ChatsRepository(context: Context) {

    private val chatsApi: ChatsApi = ApiClient.create(context, ChatsApi::class.java)
    private val userApi: UserApi = ApiClient.create(context, UserApi::class.java)

    suspend fun getChats(): List<ChatListItem> = chatsApi.getChats().data

    suspend fun getMessages(chat: ChatListItem): List<ChatMessage> =
        chatsApi.getMessages(chat.initiatorId, chat.responderId).data

    suspend fun sendMessage(chat: ChatListItem, text: String): ChatMessage {
        val response = chatsApi.sendMessage(
            chat.initiatorId,
            chat.responderId,
            SendMessageRequest(text),
        )
        return ChatMessage(
            authorId = response.authorId,
            text = response.text,
            isRead = response.isRead,
            createdAt = response.createdAt,
        )
    }

    suspend fun markRead(chat: ChatListItem) {
        chatsApi.markRead(chat.initiatorId, chat.responderId)
    }

    suspend fun getUser(userId: String): PublicUserProfile = userApi.getUser(userId)
}
