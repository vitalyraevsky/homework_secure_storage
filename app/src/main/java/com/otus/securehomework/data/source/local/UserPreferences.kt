package com.otus.securehomework.data.source.local

import android.content.Context
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import com.otus.securehomework.data.source.secure.TextCipher
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.withContext
import javax.inject.Inject

private const val dataStoreFile: String = "securePref"

class UserPreferences
@Inject constructor(
    private val context: Context,
    private val textCipher: TextCipher
) {

    val accessToken: Flow<String?>
        get() = context.dataStore.data.map { preferences ->
            preferences[ACCESS_TOKEN]?.let { encryptedToken ->
                // расшифровать
                textCipher.decrypt(encryptedToken)
            }
        }

    val refreshToken: Flow<String?>
        get() = context.dataStore.data.map { preferences ->
            preferences[REFRESH_TOKEN]?.let { encryptedToken ->
                // расшифровать
                textCipher.decrypt(encryptedToken)
            }
        }

    suspend fun saveAccessTokens(accessToken: String?, refreshToken: String?) {
        context.dataStore.edit { preferences ->
            // зашифровать токены
            withContext(Dispatchers.IO) {
                accessToken?.also {
                    preferences[ACCESS_TOKEN] = textCipher.encrypt(it)
                }
                refreshToken?.also {
                    preferences[REFRESH_TOKEN] = textCipher.encrypt(it)
                }
            }
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