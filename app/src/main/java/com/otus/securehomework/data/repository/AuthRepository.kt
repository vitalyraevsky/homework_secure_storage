package com.otus.securehomework.data.repository

import android.util.Log
import com.otus.securehomework.data.Response
import com.otus.securehomework.data.dto.LoginResponse
import com.otus.securehomework.data.source.local.UserPreferences
import com.otus.securehomework.data.source.network.AuthApi
import com.otus.securehomework.data.source.secure.PreferenceManager
import com.otus.securehomework.data.source.secure.Security
import kotlinx.coroutines.flow.first
import javax.inject.Inject

class AuthRepository
@Inject constructor(
    private val api: AuthApi,
    private val preferenceManager: PreferenceManager
) : BaseRepository(api) {

    suspend fun login(
        email: String,
        password: String
    ): Response<LoginResponse> {
        return safeApiCall { api.login(email, password) }
    }

    suspend fun saveAccessTokens(accessToken: String, refreshToken: String) {
        preferenceManager.saveAccessTokens(accessToken, refreshToken)
    }

    suspend fun getDecryptedAccessToken(): String? = preferenceManager.getDecryptedAccessToken()
}