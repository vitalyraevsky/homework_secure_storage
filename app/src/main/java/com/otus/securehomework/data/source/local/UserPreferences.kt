package com.otus.securehomework.data.source.local

import android.content.Context
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map
import javax.inject.Inject

private const val dataStoreFile: String = "securePref"

class UserPreferences
@Inject constructor(
    private val context: Context,
    private val security: Security
) {

    private val accessToken: Flow<String?>
        get() = context.dataStore.data.map { preferences ->
            preferences[ACCESS_TOKEN]
        }

    private val refreshToken: Flow<String?>
        get() = context.dataStore.data.map { preferences ->
            preferences[REFRESH_TOKEN]
        }

    suspend fun getDecryptedAccessToken(): String? {
        val accessToken = accessToken.first()
        return accessToken?.let { security.decryptAes(it) }
    }

    suspend fun getDecryptedRefreshToken(): String? {
        val refreshToken = refreshToken.first()
        return refreshToken?.let { security.decryptAes(it) }
    }

    suspend fun encryptAndSaveAccessTokens(accessToken: String, refreshToken: String) {
        val encryptedAccessToken = security.encryptAes(accessToken)
        val encryptedRefreshToken = security.encryptAes(refreshToken)
        saveAccessTokens(encryptedAccessToken, encryptedRefreshToken)
    }

    private suspend fun saveAccessTokens(accessToken: String?, refreshToken: String?) {
        context.dataStore.edit { preferences ->
            accessToken?.let { preferences[ACCESS_TOKEN] = it }
            refreshToken?.let { preferences[REFRESH_TOKEN] = it }
        }
    }

    suspend fun clear() {
        context.dataStore.edit { preferences ->
            preferences.clear()
        }
    }

    companion object {
        private val Context.dataStore by preferencesDataStore(name = dataStoreFile)
        private val ACCESS_TOKEN = stringPreferencesKey("key_access_token")
        private val REFRESH_TOKEN = stringPreferencesKey("key_refresh_token")
    }
}