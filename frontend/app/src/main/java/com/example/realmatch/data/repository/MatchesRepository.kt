package com.example.realmatch.data.repository

import android.content.Context
import com.example.realmatch.data.api.ApiClient
import com.example.realmatch.data.api.MatchesApi
import com.example.realmatch.data.api.UserApi
import com.example.realmatch.data.models.MatchItem
import com.example.realmatch.data.models.PublicUserProfile

class MatchesRepository(context: Context) {

    private val matchesApi: MatchesApi = ApiClient.create(context, MatchesApi::class.java)
    private val userApi: UserApi = ApiClient.create(context, UserApi::class.java)

    suspend fun getMatches(): List<MatchItem> = matchesApi.getMatches().data

    suspend fun getUser(userId: String): PublicUserProfile = userApi.getUser(userId)
}
