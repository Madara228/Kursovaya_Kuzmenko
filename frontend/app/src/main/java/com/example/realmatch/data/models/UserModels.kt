package com.example.realmatch.data.models

import com.google.gson.annotations.SerializedName

data class PhotoDto(
    val id: String,
    val url: String,
    @SerializedName("is_main") val isMain: Boolean = false,
)

data class UserProfile(
    val id: String,
    @SerializedName("esia_id") val esiaId: String,
    @SerializedName("first_name") val firstName: String,
    @SerializedName("last_name") val lastName: String,
    @SerializedName("birth_date") val birthDate: String,
    val gender: String,
    val city: String? = null,
    val bio: String? = null,
    val photos: List<PhotoDto> = emptyList(),
)

data class UpdateProfileRequest(
    val city: String? = null,
    val bio: String? = null,
)

data class MessageResponse(val message: String)
