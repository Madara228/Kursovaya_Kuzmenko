package com.example.realmatch.utils

import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test
import java.time.LocalDate

class ProfileFormattingTest {

    @Test
    fun `age is calculated from iso birth date`() {
        val thirtyYearsAgo = LocalDate.now().minusYears(30).toString()

        assertEquals(30, calculateAge(thirtyYearsAgo))
        assertNull(calculateAge("not-a-date"))
    }

    @Test
    fun `birth date is formatted in russian`() {
        assertEquals("20 мая 2000", formatBirthDate("2000-05-20"))
        assertNull(formatBirthDate(""))
    }

    @Test
    fun `gender is translated`() {
        assertEquals("Женщина", genderLabel("F"))
        assertEquals("Мужчина", genderLabel("M"))
    }

    @Test
    fun `relative media path becomes absolute url`() {
        val url = mediaUrl("/media/user_1/1.jpg")

        assertTrue(url!!.startsWith("http"))
        assertTrue(url.endsWith("/media/user_1/1.jpg"))
        assertEquals("https://cdn.example/photo.jpg", mediaUrl("https://cdn.example/photo.jpg"))
        assertNull(mediaUrl(null))
    }
}
