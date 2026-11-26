package com.example.realmatch.data.models

import com.google.gson.annotations.SerializedName

data class PartnerProfile(
    val id: String,
    val login: String,
    @SerializedName("contact_person") val contactPerson: String,
    val phone: String,
    val name: String,
    val city: String,
    val address: String,
    @SerializedName("contact_info") val contactInfo: String? = null,
)

data class PartnerOffer(
    val id: String,
    @SerializedName("partner_id") val partnerId: String,
    val title: String,
    @SerializedName("discount_info") val discountInfo: String,
    @SerializedName("valid_until") val validUntil: String,
    @SerializedName("photo_url") val photoUrl: String,
)
