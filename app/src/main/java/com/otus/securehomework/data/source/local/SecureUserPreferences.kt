package com.otus.securehomework.data.source.local

import android.content.Context
import androidx.security.crypto.MasterKey
import com.otus.securehomework.data.dto.LoginData
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

    val biometricData: Flow<String>
        get() = flowOf(getString(BIOMETRIC_DATA))

    val tempLoginData: Flow<LoginData>
        get() = flowOf(LoginData(getString(TEMP_EMAIL), getString(TEMP_PASSWORD)))

    val iv: Flow<String>
        get() = flowOf(getString(IV))

    fun saveAccessTokens(accessToken: String?, refreshToken: String?) {
        accessToken?.let { put(ACCESS_TOKEN, it) }
        refreshToken?.let { put(REFRESH_TOKEN, it) }
    }

    fun saveBiometricData(data: String) {
        put(BIOMETRIC_DATA, data)
    }

    fun saveIv(iv: String) {
        put(IV, iv)
    }

    fun saveTempLoginData(data: LoginData) {
        put(TEMP_EMAIL, data.email)
        put(TEMP_PASSWORD, data.password)
    }

    fun removeBiometricData() = remove(BIOMETRIC_DATA)

    fun clear() {
        remove(ACCESS_TOKEN)
        remove(REFRESH_TOKEN)
    }

    companion object {
        private const val ACCESS_TOKEN = "secureAccessToken"
        private const val REFRESH_TOKEN = "secureRefreshToken"
        private const val BIOMETRIC_DATA = "secureIsBiometricEnabled"
        private const val IV = "secureInitializationVector"
        private const val TEMP_EMAIL = "secureTempEmail"
        private const val TEMP_PASSWORD = "secureTempPassword"
    }
}