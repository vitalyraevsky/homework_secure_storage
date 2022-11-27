package com.otus.securehomework.data.repository

import com.otus.securehomework.data.source.local.UserPreferences
import com.otus.securehomework.data.source.network.UserApi
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

class UserRepository
@Inject constructor(
    private val api: UserApi,
    private val userPreferences: UserPreferences
) : BaseRepository(api) {

    suspend fun getUser() = safeApiCall { api.getUser() }

    suspend fun getBiometricSettings(): Flow<Boolean> = userPreferences.userBiometricAuth

    suspend fun enableBiometricAuth() {
        userPreferences.saveUserAuthBiometricAuth(true)
    }

    suspend fun disableBiometricAuth() {
        userPreferences.saveUserAuthBiometricAuth(false)
    }

}