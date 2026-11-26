package com.example.realmatch.data.models

import com.google.gson.Gson
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class InviteModelsTest {

    private val gson = Gson()

    @Test
    fun `offers list is parsed from backend json`() {
        val json = """
            {
              "data": [{
                "id": "d0000000-0000-0000-0000-000000000015",
                "partner_id": "c0000000-0000-0000-0000-000000000015",
                "title": "Чайная церемония",
                "discount_info": "Скидка 20% на сет",
                "valid_until": "2027-06-24",
                "photo_url": "/media/offers/15/main.jpg",
                "partner": {"name": "Чайная \"Лист\"", "city": "Москва", "address": "ул. Ленина, 1"}
              }],
              "meta": {"total_items": 1, "total_pages": 1, "current_page": 1, "per_page": 100}
            }
        """.trimIndent()

        val response = gson.fromJson(json, OffersResponse::class.java)
        val offer = response.data.first()

        assertEquals("Чайная церемония", offer.title)
        assertEquals("2027-06-24", offer.validUntil)
        assertEquals("Чайная \"Лист\"", offer.partner.name)
        assertEquals("Москва", offer.partner.city)
    }

    @Test
    fun `invite preview is parsed with validity`() {
        val preview = gson.fromJson(
            """{"offer_title":"Кофе 1+1","partner_name":"Кафе","initiator_name":"Иван","is_valid":true}""",
            InvitePreview::class.java,
        )

        assertTrue(preview.isValid)
        assertEquals("Иван", preview.initiatorName)
    }

    @Test
    fun `create invite request uses backend field names`() {
        val json = gson.toJson(
            CreateInviteRequest(
                offerId = "offer-1",
                initiatorId = "user-1",
                responderId = "user-2",
            )
        )

        assertTrue(json.contains("\"offer_id\":\"offer-1\""))
        assertTrue(json.contains("\"contact_initiator_id\":\"user-1\""))
        assertTrue(json.contains("\"contact_responder_id\":\"user-2\""))
    }
}
