package com.otus.securehomework.data.source.local

import android.content.Context
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import com.otus.securehomework.security.token.IToken
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.onEach
import javax.inject.Inject

private const val dataStoreFile: String = "securePref"
private const val APP_SETTINGS_DATA_STORE_FILE: String = "appSettingsDataStore"

class UserPreferences
@Inject constructor(
    private val context: Context,
    private val token: IToken,
) {

    val accessToken: Flow<String?>
        get() = context.dataStore.data.map { preferences ->
            preferences[ACCESS_TOKEN]?.let { token.decryptToken(it) }
        }

    val refreshToken: Flow<String?>
        get() = context.dataStore.data.map { preferences ->
            preferences[REFRESH_TOKEN]?.let { token.decryptToken(it) }
        }

    val isBiometricLoginEnabled: Flow<Boolean>
        get() = context.dataStore.data.map { preferences ->
            preferences[IS_BIOMETRIC_LOGIN_ENABLE] ?: false
        }

    suspend fun setBiometricEnable(isEnabled: Boolean) = context.dataStore.edit { preferences ->
        preferences[IS_BIOMETRIC_LOGIN_ENABLE] = isEnabled
    }


    suspend fun saveAccessTokens(accessToken: String?, refreshToken: String?) {
        val tokens = mapOf(
            Pair(ACCESS_TOKEN, accessToken),
            Pair(REFRESH_TOKEN, refreshToken)
        )
        context.dataStore.edit { preferences ->
            token.encryptTokens(tokens)
                .onEach { preferences.plusAssign(it) }
                .collect()
        }
    }

    suspend fun clear() {
        context.dataStore.edit { preferences ->
            if (isBiometricLoginEnabled.first()) {
                preferences.minusAssign(ACCESS_TOKEN)
            } else {
                preferences.clear()
            }
        }
    }

    companion object {
        private val Context.dataStore by preferencesDataStore(name = dataStoreFile)
        private val ACCESS_TOKEN = stringPreferencesKey("key_access_token")
        private val REFRESH_TOKEN = stringPreferencesKey("key_refresh_token")
        private val IS_BIOMETRIC_LOGIN_ENABLE = booleanPreferencesKey("key_biometric_enable")

    }
}