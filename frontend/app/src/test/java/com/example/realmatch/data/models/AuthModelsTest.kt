package com.example.realmatch.data.models

import com.google.gson.Gson
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class AuthModelsTest {

    @Test
    fun `esia login response is parsed from backend json`() {
        val json = """
            {
              "access_token": "token-123",
              "token_type": "bearer",
              "expires_in": 604800,
              "user": {
                "id": "550e8400-e29b-41d4-a716-446655440000",
                "first_name": "Иван",
                "last_name": "Иванов",
                "is_new_user": true
              }
            }
        """.trimIndent()

        val response = Gson().fromJson(json, AuthResponse::class.java)

        assertEquals("token-123", response.accessToken)
        assertEquals(604800L, response.expiresIn)
        assertEquals("Иван", response.user.firstName)
        assertTrue(response.user.isNewUser)
    }

    @Test
    fun `partner login response is parsed from backend json`() {
        val json = """
            {
              "access_token": "partner-token",
              "token_type": "bearer",
              "expires_in": 86400,
              "partner_id": "880e8400-e29b-41d4-a716-446655440000"
            }
        """.trimIndent()

        val response = Gson().fromJson(json, PartnerAuthResponse::class.java)

        assertEquals("partner-token", response.accessToken)
        assertEquals(86400L, response.expiresIn)
        assertEquals("880e8400-e29b-41d4-a716-446655440000", response.partnerId)
    }
}
