package com.example.realmatch.data.models

import com.google.gson.annotations.SerializedName

data class LoginRequest(
    val login: String,
    val password: String,
)

data class AuthUser(
    val id: String,
    @SerializedName("first_name") val firstName: String,
    @SerializedName("last_name") val lastName: String,
    @SerializedName("is_new_user") val isNewUser: Boolean = false,
)

data class AuthResponse(
    @SerializedName("access_token") val accessToken: String,
    @SerializedName("token_type") val tokenType: String,
    @SerializedName("expires_in") val expiresIn: Long,
    val user: AuthUser,
)

data class PartnerAuthResponse(
    @SerializedName("access_token") val accessToken: String,
    @SerializedName("token_type") val tokenType: String,
    @SerializedName("expires_in") val expiresIn: Long,
    @SerializedName("partner_id") val partnerId: String,
)
