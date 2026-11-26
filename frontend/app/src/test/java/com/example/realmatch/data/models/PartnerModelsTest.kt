package com.example.realmatch.data.models

import com.google.gson.Gson
import org.junit.Assert.assertEquals
import org.junit.Test

class PartnerModelsTest {

    private val gson = Gson()

    @Test
    fun `partner profile is parsed from backend json`() {
        val json = """
            {
              "id": "c0000000-0000-0000-0000-000000000001",
              "login": "cafe_1",
              "contact_person": "Олег",
              "phone": "+79001112233",
              "name": "Кафе \"Ромашка\"",
              "city": "Москва",
              "address": "ул. Ленина, 1",
              "contact_info": "vk.com/romashka"
            }
        """.trimIndent()

        val profile = gson.fromJson(json, PartnerProfile::class.java)

        assertEquals("cafe_1", profile.login)
        assertEquals("Москва", profile.city)
        assertEquals("vk.com/romashka", profile.contactInfo)
    }

    @Test
    fun `partner offers list is parsed from backend json`() {
        val json = """
            [{
              "id": "d0000000-0000-0000-0000-000000000001",
              "partner_id": "c0000000-0000-0000-0000-000000000001",
              "title": "Скидка 20% на кофе",
              "discount_info": "Покажите промокод бариста",
              "valid_until": "2027-02-04",
              "photo_url": "/media/offers/1/main.jpg"
            }]
        """.trimIndent()

        val offers = gson.fromJson(json, Array<PartnerOffer>::class.java)

        assertEquals(1, offers.size)
        assertEquals("2027-02-04", offers.first().validUntil)
        assertEquals("/media/offers/1/main.jpg", offers.first().photoUrl)
    }
}
