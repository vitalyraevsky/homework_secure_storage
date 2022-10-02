package com.otus.securehomework.data.source.local

import android.content.Context
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import com.otus.securehomework.data.crypto.TextCipher
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import javax.inject.Inject
import javax.inject.Singleton

private const val dataStoreFile: String = "securePref"

@Singleton
class UserPreferences @Inject constructor(
    @ApplicationContext
    private val context: Context,
    private val textCipher: TextCipher
) {

    val accessToken: Flow<String?>
        get() = context.dataStore.data.map { preferences ->
            preferences[ACCESS_TOKEN]?.let { textCipher.decryptAes(it) }
        }

    val refreshToken: Flow<String?>
        get() = context.dataStore.data.map { preferences ->
            preferences[REFRESH_TOKEN]?.let { textCipher.decryptAes(it) }
        }

    suspend fun saveAccessTokens(accessToken: String?, refreshToken: String?) {
        context.dataStore.edit { preferences ->
            accessToken?.let { preferences[ACCESS_TOKEN] = textCipher.encryptAes(it) }
            refreshToken?.let { preferences[REFRESH_TOKEN] = textCipher.encryptAes(it) }
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