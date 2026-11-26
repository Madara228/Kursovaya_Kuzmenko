package com.example.realmatch.data.models

import com.google.gson.Gson
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class ChatModelsTest {

    private val gson = Gson()

    @Test
    fun `chats list is parsed from backend json`() {
        val json = """
            {
              "data": [{
                "contact_initiator_id": "b0000000-0000-0000-0000-000000000001",
                "contact_responder_id": "b0000000-0000-0000-0000-000000000004",
                "other_user": {
                  "id": "b0000000-0000-0000-0000-000000000004",
                  "first_name": "Мария",
                  "main_photo": "/media/user_4/1.jpg"
                },
                "last_message": "Привет!",
                "last_message_time": "2023-11-01T15:00:00Z",
                "unread_count": 2
              }],
              "meta": {"total_items": 1, "total_pages": 1, "current_page": 1, "per_page": 20}
            }
        """.trimIndent()

        val response = gson.fromJson(json, ChatsResponse::class.java)
        val chat = response.data.first()

        assertEquals("Мария", chat.otherUser.firstName)
        assertEquals("Привет!", chat.lastMessage)
        assertEquals(2, chat.unreadCount)
    }

    @Test
    fun `messages are parsed with author and read flag`() {
        val json = """
            {
              "data": [{
                "author_id": "b0000000-0000-0000-0000-000000000001",
                "text": "Как дела?",
                "is_read": false,
                "created_at": "2023-11-01T14:55:00Z"
              }]
            }
        """.trimIndent()

        val response = gson.fromJson(json, MessagesResponse::class.java)
        val message = response.data.first()

        assertEquals("Как дела?", message.text)
        assertTrue(!message.isRead)
        assertEquals("b0000000-0000-0000-0000-000000000001", message.authorId)
    }

    @Test
    fun `send message request uses text field`() {
        val json = gson.toJson(SendMessageRequest("Привет"))

        assertEquals("""{"text":"Привет"}""", json)
    }
}
