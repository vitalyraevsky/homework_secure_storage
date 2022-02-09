package com.otus.securehomework.data.source.local

import android.content.Context
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import com.otus.myapplication.crypto.Keys
import com.otus.myapplication.crypto.Security
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import javax.inject.Inject

private const val dataStoreFile: String = "securePref"

class UserPreferences
@Inject constructor(
    private val context: Context,
    private val security: Security,
    private val keys: Keys
) {

    private val key by lazy { keys.getAesSecretKey() }

    val accessToken: Flow<String?>
        get() = context.dataStore.data.map { preferences ->
            preferences[ACCESS_TOKEN]?.let {
                security.decryptAes(it, key)
            } ?: run {
                null
            }
        }

    val refreshToken: Flow<String?>
        get() = context.dataStore.data.map { preferences ->
            preferences[REFRESH_TOKEN]?.let {
                security.decryptAes(it, key)
            } ?: run {
                null
            }
        }

    suspend fun saveAccessTokens(accessToken: String?, refreshToken: String?) {
        val encryptedAccessToken = security.encryptAes(accessToken ?: "", key)
        val encryptedRefreshToken = security.encryptAes(refreshToken ?: "", key)
        context.dataStore.edit { preferences ->
            preferences[ACCESS_TOKEN] = encryptedAccessToken
            preferences[REFRESH_TOKEN] = encryptedRefreshToken
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