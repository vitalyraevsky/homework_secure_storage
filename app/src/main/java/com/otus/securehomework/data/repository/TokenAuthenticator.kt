package com.otus.securehomework.data.repository

import android.util.Log
import com.otus.securehomework.data.crypto.SecuredTokenStorage
import com.otus.securehomework.data.dto.TokenResponse
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
    private val tokenApi: TokenRefreshApi,
    private val tokenStorage: SecuredTokenStorage
) : Authenticator, BaseRepository(tokenApi) {

    override fun authenticate(route: Route?, response: Response): Request? {
        return runBlocking {
            when (val tokenResponse = getUpdatedToken()) {
                is DataResponse.Success -> {

                    Log.d("save access token", "tokenStorage.saveAccessToken")
                    tokenStorage.saveAccessToken(tokenResponse.value.access_token)
                    tokenStorage.saveRefreshToken(tokenResponse.value.refresh_token)

                    response.request.newBuilder()
                        .header("Authorization", "Bearer ${tokenResponse.value.access_token}")
                        .build()
                }
                else -> null
            }
        }
    }

    private suspend fun getUpdatedToken(): DataResponse<TokenResponse> {
        val refreshToken = tokenStorage.getRefreshToken().first()
        return safeApiCall {
            tokenApi.refreshAccessToken(refreshToken)
        }
    }
}