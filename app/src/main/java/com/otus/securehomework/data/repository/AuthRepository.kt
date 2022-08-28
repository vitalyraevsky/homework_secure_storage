package com.otus.securehomework.data.repository

import com.otus.securehomework.data.Response
import com.otus.securehomework.data.dto.LoginResponse
import com.otus.securehomework.data.dto.User
import com.otus.securehomework.data.repository.crypto.IKeyProvider
import com.otus.securehomework.data.repository.crypto.SecureUserPreferences
import com.otus.securehomework.data.repository.crypto.Security
import com.otus.securehomework.data.source.local.UserPreferences
import com.otus.securehomework.data.source.network.AuthApi
import javax.inject.Inject

class AuthRepository
@Inject constructor(
    private val api: AuthApi,
    private val secureUserPreferences: SecureUserPreferences
) : BaseRepository(api) {

    suspend fun login(
        email: String,
        password: String
    ): Response<LoginResponse> {
        return safeApiCall { api.login(email, password) }
    }

    suspend fun saveAccessTokens(accessToken: String, refreshToken: String) {
        secureUserPreferences.saveAccessTokens(accessToken, refreshToken)
    }

}