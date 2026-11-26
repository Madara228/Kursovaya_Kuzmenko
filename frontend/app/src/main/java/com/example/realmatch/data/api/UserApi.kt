package com.example.realmatch.data.api

import com.example.realmatch.data.models.MessageResponse
import com.example.realmatch.data.models.PhotoDto
import com.example.realmatch.data.models.PublicUserProfile
import com.example.realmatch.data.models.UpdateProfileRequest
import com.example.realmatch.data.models.UserProfile
import okhttp3.MultipartBody
import retrofit2.http.Body
import retrofit2.http.DELETE
import retrofit2.http.GET
import retrofit2.http.Multipart
import retrofit2.http.PATCH
import retrofit2.http.POST
import retrofit2.http.Part
import retrofit2.http.Path

interface UserApi {

    @GET("users/me")
    suspend fun getMe(): UserProfile

    @PATCH("users/me")
    suspend fun updateMe(@Body request: UpdateProfileRequest): UserProfile

    @GET("users/{userId}")
    suspend fun getUser(@Path("userId") userId: String): PublicUserProfile
}

interface PhotosApi {

    @Multipart
    @POST("photos")
    suspend fun uploadPhoto(@Part file: MultipartBody.Part): PhotoDto

    @PATCH("photos/{photoId}/main")
    suspend fun setMainPhoto(@Path("photoId") photoId: String): MessageResponse

    @DELETE("photos/{photoId}")
    suspend fun deletePhoto(@Path("photoId") photoId: String): MessageResponse
}
