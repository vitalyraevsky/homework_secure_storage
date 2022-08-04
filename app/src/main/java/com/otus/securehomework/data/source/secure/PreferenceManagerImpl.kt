package com.otus.securehomework.data.source.secure

import com.otus.securehomework.data.source.local.UserPreferences
import com.otus.securehomework.data.source.network.AuthApi
import kotlinx.coroutines.flow.first
import javax.inject.Inject

class PreferenceManagerImpl
@Inject constructor(
    private val preferences: UserPreferences,
    private val security: Security
): PreferenceManager {
    override suspend fun saveAccessTokens(accessToken: String, refreshToken: String) {
        preferences.saveAccessTokens(accessToken, refreshToken)
    }

    override suspend fun getDecryptedAccessToken(): String? {
        val accessToken = preferences.accessToken.first()
        return accessToken?.let { security.decryptAes(it) }
    }

    override suspend fun getDecryptedRefreshToken(): String? {
        val refreshToken = preferences.refreshToken.first()
        return refreshToken?.let { security.decryptAes(it) }
    }

    override suspend fun saveEncryptedAccessTokens(accessToken: String?, refreshToken: String?) {
        preferences.saveAccessTokens(
            accessToken?.let { security.encryptAes(it) },
            refreshToken?.let { security.encryptAes(it) })
    }
}