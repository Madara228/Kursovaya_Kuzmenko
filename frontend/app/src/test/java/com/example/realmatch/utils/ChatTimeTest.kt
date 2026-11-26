package com.example.realmatch.utils

import org.junit.Assert.assertEquals
import org.junit.Test
import java.time.OffsetDateTime
import java.time.format.DateTimeFormatter

class ChatTimeTest {

    @Test
    fun `old message shows date`() {
        val time = OffsetDateTime.now().minusDays(2)
        val expected = time.format(DateTimeFormatter.ofPattern("dd.MM"))

        assertEquals(expected, formatChatTime(time.toString()))
    }

    @Test
    fun `recent message shows minutes`() {
        val time = OffsetDateTime.now().minusMinutes(5)

        assertEquals("5м", formatChatTime(time.toString()))
    }

    @Test
    fun `bubble time is hours and minutes`() {
        val time = OffsetDateTime.now().minusHours(1)
        val expected = time.format(DateTimeFormatter.ofPattern("HH:mm"))

        assertEquals(expected, formatMessageTime(time.toString()))
    }

    @Test
    fun `invalid time is empty`() {
        assertEquals("", formatChatTime(null))
        assertEquals("", formatChatTime("not-a-date"))
        assertEquals("", formatMessageTime("not-a-date"))
    }
}
