package com.example.realmatch.utils

import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Test

class InviteLinkTest {

    @Test
    fun `token is extracted from message text`() {
        val text = "Иван предлагает пойти в \"Чайная церемония\". " +
            "Если согласен — перейди по ссылке: /api/invites/eyJhbGciOi.abc-def_123"

        assertEquals("eyJhbGciOi.abc-def_123", extractInviteToken(text))
    }

    @Test
    fun `plain message has no token`() {
        assertNull(extractInviteToken("Привет! Как дела?"))
        assertNull(extractInviteToken(null))
    }
}
