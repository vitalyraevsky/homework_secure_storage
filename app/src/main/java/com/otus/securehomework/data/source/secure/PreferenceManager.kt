package com.otus.securehomework.data.source.secure

interface PreferenceManager {
    suspend fun saveAccessTokens(accessToken: String, refreshToken: String)

    suspend fun getDecryptedAccessToken(): String?

    suspend fun getDecryptedRefreshToken(): String?

    suspend fun saveEncryptedAccessTokens(accessToken: String?, refreshToken: String?)
}