package com.example.realmatch.data.models

import com.google.gson.Gson
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class FeedModelsTest {

    private val gson = Gson()

    @Test
    fun `feed response is parsed from backend json`() {
        val json = """
            {
              "data": [{
                "id": "b0000000-0000-0000-0000-000000000004",
                "first_name": "Мария",
                "birth_date": "1998-03-10",
                "city": "Казань",
                "bio": "Путешествия",
                "photos": [{"id": "p1", "url": "/media/user_4/1.jpg", "is_main": true}]
              }],
              "meta": {"total_items": 1, "total_pages": 1, "current_page": 1, "per_page": 20}
            }
        """.trimIndent()

        val response = gson.fromJson(json, FeedResponse::class.java)

        assertEquals("Мария", response.data.first().firstName)
        assertEquals("1998-03-10", response.data.first().birthDate)
        assertTrue(response.data.first().photos.first().isMain)
    }

    @Test
    fun `swipe request uses backend field names`() {
        val json = gson.toJson(SwipeRequest(targetUserId = "user-1", action = "LIKE"))

        assertTrue(json.contains("\"target_user_id\":\"user-1\""))
        assertTrue(json.contains("\"action\":\"LIKE\""))
    }

    @Test
    fun `swipe response parses is_match`() {
        val response = gson.fromJson("""{"status":"MATCHED","is_match":true}""", SwipeResponse::class.java)

        assertEquals("MATCHED", response.status)
        assertTrue(response.isMatch)
    }

    @Test
    fun `match item is parsed`() {
        val json = """
            {
              "data": [{
                "id": "b0000000-0000-0000-0000-000000000004",
                "user": {
                  "id": "b0000000-0000-0000-0000-000000000004",
                  "first_name": "Мария",
                  "photos": [{"id": "p1", "url": "/media/user_4/1.jpg", "is_main": true}]
                }
              }],
              "meta": {"total_items": 1, "total_pages": 1, "current_page": 1, "per_page": 20}
            }
        """.trimIndent()

        val response = gson.fromJson(json, MatchesResponse::class.java)

        assertEquals("Мария", response.data.first().user.firstName)
    }
}
