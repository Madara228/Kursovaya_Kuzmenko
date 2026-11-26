package com.example.realmatch.data.api

import com.example.realmatch.data.models.FeedResponse
import com.example.realmatch.data.models.MatchesResponse
import com.example.realmatch.data.models.SwipeRequest
import com.example.realmatch.data.models.SwipeResponse
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.POST
import retrofit2.http.Query

interface ContactsApi {

    @GET("feed")
    suspend fun getFeed(
        @Query("page") page: Int = 1,
        @Query("limit") limit: Int = 50,
    ): FeedResponse

    @POST("contacts/swipe")
    suspend fun swipe(@Body request: SwipeRequest): SwipeResponse
}

interface MatchesApi {

    @GET("matches")
    suspend fun getMatches(
        @Query("page") page: Int = 1,
        @Query("limit") limit: Int = 100,
    ): MatchesResponse
}
