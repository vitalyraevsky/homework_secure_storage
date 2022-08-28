package com.otus.securehomework.data.repository.crypto

import com.otus.securehomework.data.source.local.UserPreferences
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import javax.inject.Inject

class SecureUserPreferences @Inject constructor(
    private val preferences: UserPreferences,
    private val key: IKeyProvider,
    private val security: Security
) {

    val accessToken: Flow<String?>
        get() = preferences.accessToken.map { text ->
            val key = key.getAesSecretKey()
            text?.let { security.decryptAes(it, key) }
        }

    val refreshToken: Flow<String?>
        get() = preferences.refreshToken.map { text ->
            val key = key.getAesSecretKey()
            text?.let { security.decryptAes(it, key) }
        }

    suspend fun saveAccessTokens(accessToken: String?, refreshToken: String?) {
        val key = key.getAesSecretKey()
        val encryptedAccessToken = accessToken?.let { security.encryptAes(it, key) }
        val encryptedRefreshToken = refreshToken?.let { security.encryptAes(it, key) }
        preferences.saveAccessTokens(encryptedAccessToken, encryptedRefreshToken)
    }

    suspend fun clear() {
        preferences.clear()
    }

}