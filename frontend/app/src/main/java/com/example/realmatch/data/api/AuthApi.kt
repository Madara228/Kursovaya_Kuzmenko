package com.example.realmatch.data.api

import com.example.realmatch.data.models.AuthResponse
import com.example.realmatch.data.models.LoginRequest
import com.example.realmatch.data.models.PartnerAuthResponse
import retrofit2.http.Body
import retrofit2.http.POST

interface AuthApi {

    @POST("auth/esia-login")
    suspend fun esiaLogin(@Body request: LoginRequest): AuthResponse

    @POST("auth/partner-login")
    suspend fun partnerLogin(@Body request: LoginRequest): PartnerAuthResponse
}
