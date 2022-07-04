package com.otus.securehomework.data.source.local

import android.content.Context
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import com.otus.securehomework.crypto.Keys
import com.otus.securehomework.crypto.Security
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import javax.inject.Inject

private const val dataStoreFile: String = "securePref"

class UserPreferences
@Inject constructor(
    private val context: Context
) {

    private var keys = Keys(context)
    private var secure = Security(keys)

    val accessToken: Flow<CharSequence?>
        get() = context.dataStore.data.map { preferences ->
            secure.decrypt(preferences[ACCESS_TOKEN])
        }


    val refreshToken: Flow<CharSequence?>
        get() = context.dataStore.data.map { preferences ->
            secure.decrypt(preferences[REFRESH_TOKEN])
        }

    val enableBiometric: Flow<Boolean>
        get() = context.dataStore.data.map { preferences ->
            preferences[ENABLE_BIOMETRIC] ?: false
        }

    suspend fun saveAccessTokens(accessToken: CharSequence?, refreshToken: CharSequence?) {
        context.dataStore.edit { preferences ->
            accessToken?.let { preferences[ACCESS_TOKEN] = secure.encrypt(it) }
            refreshToken?.let { preferences[REFRESH_TOKEN] = secure.encrypt(it) }
        }
    }

    suspend fun saveUserBiometricSettings(enableBiometric: Boolean) {
        context.dataStore.edit { preferences ->
            enableBiometric.let { preferences[ENABLE_BIOMETRIC] = enableBiometric }
        }
    }

    suspend fun clear() {
        context.dataStore.edit { preferences ->
            preferences.clear()
            secure.removeKeys()
        }
    }

    companion object {
        private val Context.dataStore by preferencesDataStore(name = dataStoreFile)
        private val ACCESS_TOKEN = stringPreferencesKey("key_access_tokenn")
        private val REFRESH_TOKEN = stringPreferencesKey("key_refresh_tokenn")
        private val ENABLE_BIOMETRIC = booleanPreferencesKey("enable_biometric")

    }
}