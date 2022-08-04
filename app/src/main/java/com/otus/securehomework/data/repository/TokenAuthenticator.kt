package com.otus.securehomework.data.repository

import com.otus.securehomework.data.dto.TokenResponse
import com.otus.securehomework.data.source.local.UserPreferences
import com.otus.securehomework.data.source.network.TokenRefreshApi
import com.otus.securehomework.data.source.secure.PreferenceManager
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.runBlocking
import okhttp3.Authenticator
import okhttp3.Request
import okhttp3.Response
import okhttp3.Route
import javax.inject.Inject
import com.otus.securehomework.data.Response as DataResponse

class TokenAuthenticator @Inject constructor(
    private val _tokenApi: TokenRefreshApi,
    private val _preferenceManager: PreferenceManager
) : Authenticator, BaseRepository(_tokenApi) {

    override fun authenticate(route: Route?, response: Response): Request? {
        return runBlocking {
            when (val tokenResponse = getUpdatedToken()) {
                is DataResponse.Success -> {
                    _preferenceManager.saveEncryptedAccessTokens(
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
        val refreshToken = _preferenceManager.getDecryptedRefreshToken()
        return safeApiCall {
            _tokenApi.refreshAccessToken(refreshToken)
        }
    }
}