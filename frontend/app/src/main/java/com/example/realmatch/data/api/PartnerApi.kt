package com.example.realmatch.data.api

import com.example.realmatch.data.models.MessageResponse
import com.example.realmatch.data.models.PartnerOffer
import com.example.realmatch.data.models.PartnerProfile
import okhttp3.MultipartBody
import okhttp3.RequestBody
import retrofit2.http.DELETE
import retrofit2.http.GET
import retrofit2.http.Multipart
import retrofit2.http.PATCH
import retrofit2.http.POST
import retrofit2.http.Part
import retrofit2.http.Path

interface PartnerApi {

    @GET("partners/me")
    suspend fun getMe(): PartnerProfile

    @GET("partners/me/offers")
    suspend fun getOffers(): List<PartnerOffer>

    @Multipart
    @POST("partners/me/offers")
    suspend fun createOffer(
        @Part("title") title: RequestBody,
        @Part("discount_info") discountInfo: RequestBody,
        @Part("valid_until") validUntil: RequestBody,
        @Part photo: MultipartBody.Part,
    ): PartnerOffer

    @Multipart
    @PATCH("partners/me/offers/{offerId}")
    suspend fun updateOffer(
        @Path("offerId") offerId: String,
        @Part("title") title: RequestBody?,
        @Part("discount_info") discountInfo: RequestBody?,
        @Part("valid_until") validUntil: RequestBody?,
        @Part photo: MultipartBody.Part?,
    ): PartnerOffer

    @DELETE("partners/me/offers/{offerId}")
    suspend fun deactivateOffer(@Path("offerId") offerId: String): MessageResponse
}
