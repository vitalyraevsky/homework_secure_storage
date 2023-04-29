package com.otus.securehomework.data.source.local

import android.content.Context
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import com.otus.securehomework.data.crypto.Encryptor
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import javax.inject.Inject

private const val dataStoreFile: String = "securePref"

class UserPreferences
@Inject constructor(
    private val context: Context,
    private val encryptor: Encryptor
) {

    val accessToken: Flow<String?>
        get() = context.dataStore.data.map { preferences ->
            preferences[ACCESS_TOKEN]?.let {
                encryptor.decrypt(it)
            }
        }

    val refreshToken: Flow<String?>
        get() = context.dataStore.data.map { preferences ->
            preferences[REFRESH_TOKEN]?.let {
                encryptor.decrypt(it)
            }
        }

    val authWithBiometry: Flow<Boolean>
        get() = context.dataStore.data.map { preferences ->
            preferences[AUTH_WITH_BIOMETRY_TOKEN] ?: false
        }

    suspend fun saveAccessTokens(accessToken: String?, refreshToken: String?) {
        context.dataStore.edit { preferences ->
            accessToken?.let {
                preferences[ACCESS_TOKEN] = encryptor.encrypt(it)
            }
            refreshToken?.let {
                preferences[REFRESH_TOKEN] = encryptor.encrypt(it)
            }
        }
    }

    suspend fun changeBiometrySetting(isBiometryAuthEnabled: Boolean) {
        context.dataStore.edit { preferences ->
            preferences[AUTH_WITH_BIOMETRY_TOKEN] = isBiometryAuthEnabled
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
        private val AUTH_WITH_BIOMETRY_TOKEN = booleanPreferencesKey("key_auth_with_biometry")
    }
}