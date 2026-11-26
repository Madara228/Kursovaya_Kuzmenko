package com.example.realmatch.data.repository

import android.content.Context
import android.net.Uri
import com.example.realmatch.data.api.ApiClient
import com.example.realmatch.data.api.PhotosApi
import com.example.realmatch.data.api.UserApi
import com.example.realmatch.data.models.PhotoDto
import com.example.realmatch.data.models.UpdateProfileRequest
import com.example.realmatch.data.models.UserProfile
import com.example.realmatch.utils.uriToMultipart

class UserRepository(private val context: Context) {

    private val userApi: UserApi = ApiClient.create(context, UserApi::class.java)
    private val photosApi: PhotosApi = ApiClient.create(context, PhotosApi::class.java)

    suspend fun getMe(): UserProfile = userApi.getMe()

    suspend fun updateProfile(city: String, bio: String): UserProfile =
        userApi.updateMe(UpdateProfileRequest(city = city, bio = bio))

    suspend fun uploadPhoto(uri: Uri): PhotoDto =
        photosApi.uploadPhoto(uriToMultipart(context, uri, partName = "file"))

    suspend fun setMainPhoto(photoId: String) {
        photosApi.setMainPhoto(photoId)
    }

    suspend fun deletePhoto(photoId: String) {
        photosApi.deletePhoto(photoId)
    }
}
