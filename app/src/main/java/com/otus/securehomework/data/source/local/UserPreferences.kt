package com.otus.securehomework.data.source.local

import android.content.Context
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import com.otus.securehomework.data.security.Crypto
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.withContext
import javax.inject.Inject

class UserPreferences @Inject constructor(
    private val context: Context,
    private val crypto: Crypto,
) {

    private companion object {
        const val DATA_STORE_FILE: String = "securePref"

        val Context.dataStore by preferencesDataStore(name = DATA_STORE_FILE)
        val ACCESS_TOKEN = stringPreferencesKey("key_access_token")
        val REFRESH_TOKEN = stringPreferencesKey("key_refresh_token")
    }

    val accessToken: Flow<String?>
        get() = context.dataStore.data.map { preferences ->
            withContext(Dispatchers.IO) {
                preferences[ACCESS_TOKEN]?.let { crypto.decrypt(it) }
            }
        }

    val refreshToken: Flow<String?>
        get() = context.dataStore.data.map { preferences ->
            withContext(Dispatchers.IO) {
                preferences[REFRESH_TOKEN]?.let { crypto.decrypt(it) }
            }
        }

    suspend fun saveAccessTokens(accessToken: String?, refreshToken: String?) {
        context.dataStore.edit { preferences ->
            withContext(Dispatchers.IO) {
                accessToken?.let { preferences[ACCESS_TOKEN] = crypto.encrypt(it) }
                refreshToken?.let { preferences[REFRESH_TOKEN] = crypto.encrypt(it) }
            }
        }
    }

    suspend fun clear() {
        context.dataStore.edit { preferences ->
            preferences.clear()
        }
    }
}