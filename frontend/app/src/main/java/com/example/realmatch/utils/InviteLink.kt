package com.example.realmatch.utils

import android.util.Base64
import com.google.gson.Gson
import com.google.gson.annotations.SerializedName

private val inviteLinkRegex = Regex("""/api/invites/([A-Za-z0-9._-]+)""")

/** Достаёт JWT-токен из текста сообщения-приглашения. */
fun extractInviteToken(text: String?): String? =
    text?.let { inviteLinkRegex.find(it)?.groupValues?.getOrNull(1) }

data class InviteParticipants(
    val senderId: String?,
    val receiverId: String?,
)

private data class InvitePayload(
    @SerializedName("sender_id") val senderId: String? = null,
    @SerializedName("receiver_id") val receiverId: String? = null,
)

/** Читает sender_id/receiver_id из payload JWT, чтобы понять, чьё это приглашение. */
fun decodeInviteParticipants(token: String): InviteParticipants? {
    val payload = token.split(".").getOrNull(1) ?: return null
    return runCatching {
        val json = String(
            Base64.decode(payload, Base64.URL_SAFE or Base64.NO_WRAP or Base64.NO_PADDING),
            Charsets.UTF_8,
        )
        val parsed = Gson().fromJson(json, InvitePayload::class.java)
        InviteParticipants(senderId = parsed.senderId, receiverId = parsed.receiverId)
    }.getOrNull()
}
