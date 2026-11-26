package com.example.realmatch.data.repository

import android.content.Context
import com.example.realmatch.data.api.ApiClient
import com.example.realmatch.data.api.InvitesApi
import com.example.realmatch.data.models.AcceptInviteResponse
import com.example.realmatch.data.models.CreateInviteRequest
import com.example.realmatch.data.models.CreateInviteResponse
import com.example.realmatch.data.models.InvitePreview
import com.example.realmatch.data.models.Offer

class InviteRepository(context: Context) {

    private val api: InvitesApi = ApiClient.create(context, InvitesApi::class.java)

    suspend fun getOffers(): List<Offer> = api.getOffers().data

    suspend fun createInvite(
        offerId: String,
        initiatorId: String,
        responderId: String,
    ): CreateInviteResponse = api.createInvite(
        CreateInviteRequest(
            offerId = offerId,
            initiatorId = initiatorId,
            responderId = responderId,
        )
    )

    suspend fun getPreview(token: String): InvitePreview = api.getInvitePreview(token)

    suspend fun accept(token: String): AcceptInviteResponse = api.acceptInvite(token)
}
