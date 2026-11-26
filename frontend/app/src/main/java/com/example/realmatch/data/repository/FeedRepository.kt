package com.example.realmatch.data.repository

import android.content.Context
import com.example.realmatch.data.api.ApiClient
import com.example.realmatch.data.api.ContactsApi
import com.example.realmatch.data.api.UserApi
import com.example.realmatch.data.models.FeedUser
import com.example.realmatch.data.models.SwipeRequest
import com.example.realmatch.data.models.SwipeResponse
import com.example.realmatch.data.models.UserProfile
import com.example.realmatch.utils.mediaUrl

class FeedRepository(context: Context) {

    private val contactsApi: ContactsApi = ApiClient.create(context, ContactsApi::class.java)
    private val userApi: UserApi = ApiClient.create(context, UserApi::class.java)

    suspend fun getFeed(): List<FeedUser> = contactsApi.getFeed().data

    suspend fun swipe(targetUserId: String, action: String): SwipeResponse =
        contactsApi.swipe(SwipeRequest(targetUserId = targetUserId, action = action))

    suspend fun getMyMainPhoto(): String? {
        val profile: UserProfile = userApi.getMe()
        val main = profile.photos.firstOrNull { it.isMain } ?: profile.photos.firstOrNull()
        return mediaUrl(main?.url)
    }
}
