package com.example.realmatch.data.repository

import android.content.Context
import android.net.Uri
import com.example.realmatch.data.api.ApiClient
import com.example.realmatch.data.api.PartnerApi
import com.example.realmatch.data.models.PartnerOffer
import com.example.realmatch.data.models.PartnerProfile
import com.example.realmatch.utils.uriToMultipart
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.RequestBody
import okhttp3.RequestBody.Companion.toRequestBody

class PartnerRepository(private val context: Context) {

    private val api: PartnerApi = ApiClient.create(context, PartnerApi::class.java)

    suspend fun getMe(): PartnerProfile = api.getMe()

    suspend fun getOffers(): List<PartnerOffer> = api.getOffers()

    suspend fun createOffer(
        title: String,
        discountInfo: String,
        validUntilIso: String,
        photoUri: Uri,
    ): PartnerOffer = api.createOffer(
        title = title.asTextPart(),
        discountInfo = discountInfo.asTextPart(),
        validUntil = validUntilIso.asTextPart(),
        photo = uriToMultipart(context, photoUri, partName = "photo"),
    )

    suspend fun updateOffer(
        offerId: String,
        title: String,
        discountInfo: String,
        validUntilIso: String,
        photoUri: Uri?,
    ): PartnerOffer = api.updateOffer(
        offerId = offerId,
        title = title.asTextPart(),
        discountInfo = discountInfo.asTextPart(),
        validUntil = validUntilIso.asTextPart(),
        photo = photoUri?.let { uriToMultipart(context, it, partName = "photo") },
    )

    suspend fun deactivateOffer(offerId: String) {
        api.deactivateOffer(offerId)
    }

    private fun String.asTextPart(): RequestBody = toRequestBody("text/plain".toMediaType())
}
