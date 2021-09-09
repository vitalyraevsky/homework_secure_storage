package com.otus.securehomework.data.source.local

import android.content.Context
import androidx.security.crypto.MasterKey
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flowOf
import javax.inject.Inject

class SecureUserPreferences @Inject constructor(
    context: Context, masterKey: MasterKey
) : BaseSecurePreferences(context, masterKey) {

    val accessToken: Flow<String>
        get() = flowOf(getString(ACCESS_TOKEN))

    val refreshToken: Flow<String>
        get() = flowOf(getString(REFRESH_TOKEN))

    fun saveAccessTokens(accessToken: String?, refreshToken: String?) {
        accessToken?.let { put(ACCESS_TOKEN, it) }
        refreshToken?.let { put(ACCESS_TOKEN, it) }
    }

    fun clear() {
        remove(ACCESS_TOKEN)
        remove(REFRESH_TOKEN)
    }

    companion object {
        private const val ACCESS_TOKEN = "secureAccessToken"
        private const val REFRESH_TOKEN = "secureRefreshToken"
    }
}