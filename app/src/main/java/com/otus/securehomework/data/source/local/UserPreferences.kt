package com.otus.securehomework.data.source.local

import android.content.Context
import android.util.Log
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import com.otus.securehomework.data.crypto.AppKeyGenerator
import com.otus.securehomework.data.crypto.Secure
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import javax.inject.Inject

private const val dataStoreFile: String = "securePref"

class UserPreferences
@Inject constructor(
    private val context: Context,
    private val keys: AppKeyGenerator,
    private val secure: Secure
) {

    val accessToken: Flow<String?>
        get() = context.dataStore.data.map { preferences ->
            val key = keys.getSecretKey()
            preferences[ACCESS_TOKEN]?.let {
                secure.decryptAes(it, key)
            }
        }

    val refreshToken: Flow<String?>
        get() = context.dataStore.data.map { preferences ->
            val key = keys.getSecretKey()
            preferences[REFRESH_TOKEN]?.let {
                secure.decryptAes(it, key)
            }
        }

    suspend fun saveAccessTokens(accessToken: CharSequence?, refreshToken: CharSequence?) {
        context.dataStore.edit { preferences ->

            Log.i("TAG", "saveAccessTokens: ${accessToken.toString()}   ${refreshToken.toString()}")

            accessToken?.let {
                val key = keys.getSecretKey()
                val encryptedToken = secure.encryptAes(it.toString(), key)
                Log.i("TAG", "saveAccessTokens: $encryptedToken")
                preferences[ACCESS_TOKEN] = encryptedToken
            }
            refreshToken?.let {
                val encryptedToken = secure.encryptAes(it.toString(), keys.getSecretKey())
                Log.i("TAG", "saveAccessTokens: $encryptedToken")
                preferences[REFRESH_TOKEN] = encryptedToken
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