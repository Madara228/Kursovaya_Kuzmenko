package com.example.realmatch.data.models

import com.google.gson.annotations.SerializedName

data class ChatOtherUser(
    val id: String,
    @SerializedName("first_name") val firstName: String,
    @SerializedName("main_photo") val mainPhoto: String? = null,
)

data class ChatListItem(
    @SerializedName("contact_initiator_id") val initiatorId: String,
    @SerializedName("contact_responder_id") val responderId: String,
    @SerializedName("other_user") val otherUser: ChatOtherUser,
    @SerializedName("last_message") val lastMessage: String? = null,
    @SerializedName("last_message_time") val lastMessageTime: String? = null,
    @SerializedName("unread_count") val unreadCount: Int = 0,
)

data class ChatsResponse(
    val data: List<ChatListItem> = emptyList(),
    val meta: PaginationMeta? = null,
)

data class ChatMessage(
    @SerializedName("author_id") val authorId: String,
    val text: String,
    @SerializedName("is_read") val isRead: Boolean = false,
    @SerializedName("created_at") val createdAt: String,
)

data class MessagesResponse(
    val data: List<ChatMessage> = emptyList(),
    val meta: PaginationMeta? = null,
)

data class SendMessageRequest(val text: String)

data class SendMessageResponse(
    @SerializedName("author_id") val authorId: String,
    val text: String,
    @SerializedName("is_read") val isRead: Boolean = false,
    @SerializedName("created_at") val createdAt: String,
)

data class MarkReadResponse(
    val status: String = "success",
    @SerializedName("updated_count") val updatedCount: Int = 0,
)
