package com.example.realmatch.data.api

import com.example.realmatch.data.models.AcceptInviteResponse
import com.example.realmatch.data.models.CreateInviteRequest
import com.example.realmatch.data.models.CreateInviteResponse
import com.example.realmatch.data.models.InvitePreview
import com.example.realmatch.data.models.OffersResponse
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.POST
import retrofit2.http.Path
import retrofit2.http.Query

interface InvitesApi {

    @GET("offers")
    suspend fun getOffers(
        @Query("page") page: Int = 1,
        @Query("limit") limit: Int = 100,
    ): OffersResponse

    @POST("invites")
    suspend fun createInvite(@Body request: CreateInviteRequest): CreateInviteResponse

    @GET("invites/{token}")
    suspend fun getInvitePreview(@Path("token") token: String): InvitePreview

    @POST("invites/{token}/accept")
    suspend fun acceptInvite(@Path("token") token: String): AcceptInviteResponse
}
