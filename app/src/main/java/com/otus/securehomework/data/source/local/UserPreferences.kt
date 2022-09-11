package com.otus.securehomework.data.source.local

import android.content.Context
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import com.otus.securehomework.data.crypto.Keys
import com.otus.securehomework.data.crypto.Security
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import javax.crypto.SecretKey
import javax.inject.Inject

private const val dataStoreFile: String = "securePref"

class UserPreferences @Inject constructor(
    private val context: Context,
    private val security: Security,
    private val keys: Keys
) {

    val accessToken: Flow<String?>
        get() = getDecryptedToken(ACCESS_TOKEN)

    val refreshToken: Flow<String?>
        get() = getDecryptedToken(REFRESH_TOKEN)

    suspend fun saveAccessTokens(accessToken: String?, refreshToken: String?) {
        val secretKey = keys.getAesSecretKey()
        accessToken?.let { saveEncryptedToken(ACCESS_TOKEN, it, secretKey) }
        refreshToken?.let { saveEncryptedToken(REFRESH_TOKEN, it, secretKey) }
    }

    private suspend fun saveEncryptedToken(key: Preferences.Key<String>, token: String, secretKey: SecretKey) {
        context.dataStore.edit { preferences ->
            val encryptedToken = security.encryptAes(token, secretKey)
            preferences[key] = encryptedToken
        }
    }

    private fun getDecryptedToken(key: Preferences.Key<String>): Flow<String?> {
        return context.dataStore.data.map { preferences ->
            val encryptedToken = preferences[key]
            encryptedToken?.let { security.decryptAes(it, keys.getAesSecretKey()) }
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