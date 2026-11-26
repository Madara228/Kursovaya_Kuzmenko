package com.example.realmatch.data.local

import android.content.Context
import android.content.SharedPreferences
import androidx.security.crypto.EncryptedSharedPreferences
import androidx.security.crypto.MasterKey

enum class UserRole { USER, PARTNER }

class TokenManager private constructor(context: Context) {

    private val prefs: SharedPreferences = EncryptedSharedPreferences.create(
        context,
        PREFS_NAME,
        MasterKey.Builder(context).setKeyScheme(MasterKey.KeyScheme.AES256_GCM).build(),
        EncryptedSharedPreferences.PrefKeyEncryptionScheme.AES256_SIV,
        EncryptedSharedPreferences.PrefValueEncryptionScheme.AES256_GCM,
    )

    val token: String? get() = prefs.getString(KEY_TOKEN, null)

    val role: UserRole?
        get() = prefs.getString(KEY_ROLE, null)?.let { runCatching { UserRole.valueOf(it) }.getOrNull() }

    val accountId: String? get() = prefs.getString(KEY_ACCOUNT_ID, null)

    val isLoggedIn: Boolean get() = token != null

    fun saveSession(token: String, role: UserRole, accountId: String, startOnboarding: Boolean = false) {
        prefs.edit()
            .putString(KEY_TOKEN, token)
            .putString(KEY_ROLE, role.name)
            .putString(KEY_ACCOUNT_ID, accountId)
            .putBoolean(KEY_ONBOARDING, startOnboarding)
            .apply()
    }

    /** true только для первого входа нового пользователя; флаг сбрасывается после чтения. */
    fun consumeOnboardingPending(): Boolean {
        val pending = prefs.getBoolean(KEY_ONBOARDING, false)
        if (pending) prefs.edit().putBoolean(KEY_ONBOARDING, false).apply()
        return pending
    }

    fun clear() {
        prefs.edit().clear().apply()
    }

    companion object {
        private const val PREFS_NAME = "realmatch_session"
        private const val KEY_TOKEN = "access_token"
        private const val KEY_ROLE = "role"
        private const val KEY_ACCOUNT_ID = "account_id"
        private const val KEY_ONBOARDING = "onboarding_pending"

        @Volatile
        private var instance: TokenManager? = null

        fun getInstance(context: Context): TokenManager =
            instance ?: synchronized(this) {
                instance ?: TokenManager(context.applicationContext).also { instance = it }
            }
    }
}
