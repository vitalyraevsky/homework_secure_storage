package com.otus.securehomework.data.source.local

import android.content.Context
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import com.otus.securehomework.data.security.Aes
import com.otus.securehomework.data.security.KeyGen
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import javax.inject.Inject

private const val dataStoreFile: String = "securePref"

class UserPreferences @Inject constructor(
    @ApplicationContext private val context: Context,
    private val keyGen: KeyGen,
    private val aes: Aes
) {

    val accessToken: Flow<String?>
        get() = context.dataStore.data.map { preferences ->
            preferences[ACCESS_TOKEN]
        }

    val refreshToken: Flow<String?>
        get() = context.dataStore.data.map { preferences ->
            preferences[REFRESH_TOKEN]
        }

    suspend fun saveAccessTokens(accessToken: CharSequence?, refreshToken: CharSequence?) {
        context.dataStore.edit { preferences ->
            val key = keyGen.generate()
            if (accessToken != null)
                preferences[ACCESS_TOKEN] = aes.encrypt(accessToken, key)

            if (refreshToken != null)
                preferences[REFRESH_TOKEN] = aes.encrypt(refreshToken, key)
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