package com.otus.securehomework.data.source.local

import android.content.Context
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import com.otus.securehomework.data.security.IEncryptorDecryptor
import com.otus.securehomework.data.security.IKeyGenerator
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import javax.inject.Inject
import javax.inject.Singleton

private const val dataStoreFile: String = "securePref"

@Singleton
class UserPreferences @Inject constructor(
    @ApplicationContext private val context: Context,
    private val keyGenerator: IKeyGenerator,
    private val encryptorDecryptor: IEncryptorDecryptor
) {

    val accessToken: Flow<String?>
        get() = context.dataStore.data.map { preferences ->
            val encryptAccessToken = preferences[ACCESS_TOKEN] ?: return@map null
            val key = keyGenerator.getSecretKey()
            encryptorDecryptor.decryptAes(encryptAccessToken, key)
        }

    val refreshToken: Flow<String?>
        get() = context.dataStore.data.map { preferences ->
            val encryptRefreshToken = preferences[REFRESH_TOKEN] ?: return@map null
            val key = keyGenerator.getSecretKey()
            encryptorDecryptor.decryptAes(encryptRefreshToken, key)
        }

    suspend fun saveAccessTokens(accessToken: String?, refreshToken: String?) {
        context.dataStore.edit { preferences ->
            val key = keyGenerator.getSecretKey()
            accessToken?.let { preferences[ACCESS_TOKEN] = encryptorDecryptor.encryptAes(it, key) }
            refreshToken?.let {
                preferences[REFRESH_TOKEN] = encryptorDecryptor.encryptAes(it, key)
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