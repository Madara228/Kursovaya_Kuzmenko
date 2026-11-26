package com.example.realmatch.data.models

import com.google.gson.annotations.SerializedName

data class PaginationMeta(
    @SerializedName("total_items") val totalItems: Int = 0,
    @SerializedName("total_pages") val totalPages: Int = 0,
    @SerializedName("current_page") val currentPage: Int = 1,
    @SerializedName("per_page") val perPage: Int = 20,
)

data class FeedUser(
    val id: String,
    @SerializedName("first_name") val firstName: String,
    @SerializedName("birth_date") val birthDate: String,
    val city: String? = null,
    val bio: String? = null,
    val photos: List<PhotoDto> = emptyList(),
)

data class FeedResponse(
    val data: List<FeedUser> = emptyList(),
    val meta: PaginationMeta? = null,
)

data class SwipeRequest(
    @SerializedName("target_user_id") val targetUserId: String,
    val action: String,
)

data class SwipeResponse(
    val status: String,
    @SerializedName("is_match") val isMatch: Boolean = false,
)

data class MatchUser(
    val id: String,
    @SerializedName("first_name") val firstName: String,
    val photos: List<PhotoDto> = emptyList(),
)

data class MatchItem(
    val id: String,
    val user: MatchUser,
)

data class MatchesResponse(
    val data: List<MatchItem> = emptyList(),
    val meta: PaginationMeta? = null,
)

data class PublicUserProfile(
    val id: String,
    @SerializedName("first_name") val firstName: String,
    @SerializedName("last_name") val lastName: String,
    @SerializedName("birth_date") val birthDate: String,
    val gender: String,
    val city: String? = null,
    val bio: String? = null,
    val photos: List<PhotoDto> = emptyList(),
)
