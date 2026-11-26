package com.example.realmatch.data.repository

import android.content.Context
import com.example.realmatch.data.api.ApiClient
import com.example.realmatch.data.api.AuthApi
import com.example.realmatch.data.local.TokenManager
import com.example.realmatch.data.local.UserRole
import com.example.realmatch.data.models.LoginRequest

class AuthRepository(context: Context) {

    private val api: AuthApi = ApiClient.retrofit(context).create(AuthApi::class.java)
    private val tokens: TokenManager = TokenManager.getInstance(context)

    suspend fun loginAsUser(login: String, password: String) {
        val response = api.esiaLogin(LoginRequest(login, password))
        tokens.saveSession(
            token = response.accessToken,
            role = UserRole.USER,
            accountId = response.user.id,
            startOnboarding = response.user.isNewUser,
        )
    }

    suspend fun loginAsPartner(login: String, password: String) {
        val response = api.partnerLogin(LoginRequest(login, password))
        tokens.saveSession(response.accessToken, UserRole.PARTNER, response.partnerId)
    }
}
