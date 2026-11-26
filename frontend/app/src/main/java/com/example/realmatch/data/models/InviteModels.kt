package com.example.realmatch.data.models

import com.google.gson.annotations.SerializedName

data class OfferPartner(
    val name: String,
    val city: String? = null,
    val address: String,
)

data class Offer(
    val id: String,
    @SerializedName("partner_id") val partnerId: String,
    val title: String,
    @SerializedName("discount_info") val discountInfo: String,
    @SerializedName("valid_until") val validUntil: String,
    @SerializedName("photo_url") val photoUrl: String,
    val partner: OfferPartner,
)

data class OffersResponse(
    val data: List<Offer> = emptyList(),
    val meta: PaginationMeta? = null,
)

data class CreateInviteRequest(
    @SerializedName("offer_id") val offerId: String,
    @SerializedName("contact_initiator_id") val initiatorId: String,
    @SerializedName("contact_responder_id") val responderId: String,
)

data class CreateInviteResponse(
    @SerializedName("invite_link") val inviteLink: String,
    @SerializedName("message_id") val messageId: String,
)

data class InvitePreview(
    @SerializedName("offer_title") val offerTitle: String,
    @SerializedName("partner_name") val partnerName: String,
    @SerializedName("initiator_name") val initiatorName: String,
    @SerializedName("is_valid") val isValid: Boolean,
)

data class AcceptInviteResponse(
    val status: String,
    @SerializedName("promocode_id") val promocodeId: String,
)
