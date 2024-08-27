package com.otus.securehomework.data.repository

import com.otus.securehomework.data.Response
import com.otus.securehomework.data.dto.LoginResponse
import com.otus.securehomework.data.source.local.UserPreferences
import com.otus.securehomework.data.source.network.AuthApi
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.last
import javax.inject.Inject

class AuthRepository
@Inject constructor(
    private val api: AuthApi,
    private val preferences: UserPreferences,
    private val securityRepository: SecurityRepository
) : BaseRepository(api) {


    suspend fun login(
        email: String,
        password: String
    ): Response<LoginResponse> {
        return safeApiCall { api.login(email, password) }
    }

    suspend fun saveAccessTokens(accessToken: String, refreshToken: String) {
        preferences.saveAccessTokens(securityRepository.encryptAes(accessToken), securityRepository.encryptAes(refreshToken))
    }

    suspend fun isLoggedIn(): Flow<Boolean> {
       return flowOf(!preferences.accessToken.last().isNullOrEmpty() && !preferences.refreshToken.last().isNullOrEmpty())
    }
}