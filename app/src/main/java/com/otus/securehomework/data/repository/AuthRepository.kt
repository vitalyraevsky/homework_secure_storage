package com.otus.securehomework.data.repository

import com.otus.securehomework.data.Response
import com.otus.securehomework.data.dto.LoginResponse
import com.otus.securehomework.data.source.crypto.Security
import com.otus.securehomework.data.source.local.UserPreferences
import com.otus.securehomework.data.source.network.AuthApi
import kotlinx.coroutines.flow.first
import javax.inject.Inject

class AuthRepository
@Inject constructor(
    private val api: AuthApi,
    private val preferences: UserPreferences,
    private val security: Security
) : BaseRepository(api) {

    suspend fun login(
        email: String,
        password: String
    ): Response<LoginResponse> {
        return safeApiCall { api.login(email, password) }
    }

    suspend fun getDecryptedAccessToken(): String? {
        val accessToken = preferences.accessToken.first()
        return accessToken?.let { security.decryptAes(it) }
    }

    suspend fun getDecryptedRefreshToken(): String? {
        val refreshToken = preferences.refreshToken.first()
        return refreshToken?.let { security.decryptAes(it) }
    }

    suspend fun saveEncryptedAccessTokens(accessToken: String, refreshToken: String) {
        val encryptedAccessToken = security.encryptAes(accessToken)
        val encryptedRefreshToken = security.encryptAes(refreshToken)
        preferences.saveAccessTokens(encryptedAccessToken, encryptedRefreshToken)
    }

    suspend fun saveAccessTokens(accessToken: String, refreshToken: String) {
        preferences.saveAccessTokens(accessToken, refreshToken)
    }
}