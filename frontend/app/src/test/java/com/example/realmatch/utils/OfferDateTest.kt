package com.example.realmatch.utils

import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Test
import java.time.LocalDate

class OfferDateTest {

    @Test
    fun `input date is parsed and converted to iso`() {
        assertEquals("2027-12-31", offerDateInputToIso("31.12.2027"))
        assertEquals(LocalDate.of(2027, 12, 31), parseOfferDateInput("31.12.2027"))
    }

    @Test
    fun `iso date is converted for input and display`() {
        assertEquals("31.12.2027", offerDateIsoToInput("2027-12-31"))
        assertEquals("24.06.2027", formatOfferDate("2027-06-24"))
    }

    @Test
    fun `invalid date returns null`() {
        assertNull(offerDateInputToIso("31/12/2027"))
        assertEquals("not-a-date", offerDateIsoToInput("not-a-date"))
    }
}
