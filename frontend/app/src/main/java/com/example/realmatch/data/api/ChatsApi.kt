package com.example.realmatch.data.api

import com.example.realmatch.data.models.ChatsResponse
import com.example.realmatch.data.models.MarkReadResponse
import com.example.realmatch.data.models.MessagesResponse
import com.example.realmatch.data.models.SendMessageRequest
import com.example.realmatch.data.models.SendMessageResponse
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.PATCH
import retrofit2.http.POST
import retrofit2.http.Path
import retrofit2.http.Query

interface ChatsApi {

    @GET("chats")
    suspend fun getChats(
        @Query("page") page: Int = 1,
        @Query("limit") limit: Int = 100,
    ): ChatsResponse

    @GET("chats/{initiatorId}/{responderId}/messages")
    suspend fun getMessages(
        @Path("initiatorId") initiatorId: String,
        @Path("responderId") responderId: String,
        @Query("page") page: Int = 1,
        @Query("limit") limit: Int = 100,
    ): MessagesResponse

    @POST("chats/{initiatorId}/{responderId}/messages")
    suspend fun sendMessage(
        @Path("initiatorId") initiatorId: String,
        @Path("responderId") responderId: String,
        @Body request: SendMessageRequest,
    ): SendMessageResponse

    @PATCH("chats/{initiatorId}/{responderId}/read")
    suspend fun markRead(
        @Path("initiatorId") initiatorId: String,
        @Path("responderId") responderId: String,
    ): MarkReadResponse
}
