package com.otus.securehomework.data.source.local

import android.content.Context
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import com.otus.securehomework.data.myDefence.Security
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.launch
import javax.inject.Inject
import kotlin.coroutines.suspendCoroutine

private const val dataStoreFile: String = "securePref"

class UserPreferences
@Inject constructor(
    private val context: Context,
    private val security: Security,
) {

    val accessToken: Flow<String?>
        get() = context.dataStore.data.map { preferences ->
            preferences[ACCESS_TOKEN]
            //getDecryptedAccessToken(preferences[ACCESS_TOKEN])
        }

    val refreshToken: Flow<String?>
        get() = context.dataStore.data.map { preferences ->
            preferences[REFRESH_TOKEN]
        }

    suspend fun saveAccessTokens(accessToken: String?, refreshToken: String?) {
        context.dataStore.edit { preferences ->
            accessToken?.let { preferences[ACCESS_TOKEN] = it }
            refreshToken?.let { preferences[REFRESH_TOKEN] = it }
        }
    }

    suspend fun saveEncryptedAccessTokens(accessToken: String?, refreshToken: String?) {
        context.dataStore.edit { preferences ->
            accessToken?.let { preferences[ACCESS_TOKEN] = security.encryptAes(it) }
            refreshToken?.let { preferences[REFRESH_TOKEN] = security.encryptAes(it) }
        }
    }

    suspend fun getDecryptedAccessToken(accessToken: String?): String? {
        return accessToken?.let { security.decryptAes(it) }
    }

    suspend fun getDecryptedRefreshToken(): String? {
        val refreshToken = refreshToken.first()
        return refreshToken?.let { security.decryptAes(it) }
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