package com.otus.securehomework.data.repository

import android.content.Context
import com.otus.securehomework.data.dto.TokenResponse
import com.otus.securehomework.data.source.local.SecureUserPreferences
import com.otus.securehomework.data.source.local.UserPreferences
import com.otus.securehomework.data.source.network.TokenRefreshApi
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.runBlocking
import okhttp3.Authenticator
import okhttp3.Request
import okhttp3.Response
import okhttp3.Route
import javax.inject.Inject
import com.otus.securehomework.data.Response as DataResponse


class TokenAuthenticator @Inject constructor(
    context: Context,
    private val tokenApi: TokenRefreshApi,
    private val userPreferences: SecureUserPreferences
) : Authenticator, BaseRepository(tokenApi) {

    override fun authenticate(route: Route?, response: Response): Request? {
        return runBlocking {
            when (val tokenResponse = getUpdatedToken()) {
                is DataResponse.Success -> {
                    userPreferences.saveAccessTokens(
                        tokenResponse.value.access_token,
                        tokenResponse.value.refresh_token
                    )
                    response.request.newBuilder()
                        .header("Authorization", "Bearer ${tokenResponse.value.access_token}")
                        .build()
                }
                else -> null
            }
        }
    }

    private suspend fun getUpdatedToken(): DataResponse<TokenResponse> {
        val refreshToken = userPreferences.refreshToken.first()
        return safeApiCall {
            tokenApi.refreshAccessToken(refreshToken)
        }
    }
}