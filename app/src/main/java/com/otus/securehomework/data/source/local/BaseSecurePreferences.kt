package com.otus.securehomework.data.source.local

import android.content.Context
import androidx.security.crypto.EncryptedSharedPreferences
import androidx.security.crypto.MasterKey


abstract class BaseSecurePreferences(context: Context, masterKey: MasterKey) {

    private val preferences by lazy {
        EncryptedSharedPreferences.create(
            context, SECURE_PREF_NAME, masterKey,
            EncryptedSharedPreferences.PrefKeyEncryptionScheme.AES256_SIV,
            EncryptedSharedPreferences.PrefValueEncryptionScheme.AES256_GCM
        )
    }

    fun put(key: String, value: String) = preferences.edit().putString(key, value).commit()
    fun put(key: String, value: Boolean) = preferences.edit().putBoolean(key, value).commit()

    fun getString(key: String, defValue: String = ""): String =
        preferences.getString(key, defValue) ?: defValue

    fun getBoolean(key: String, defValue: Boolean = false): Boolean = preferences.getBoolean(key, defValue)

    fun remove(key: String) = preferences.edit().remove(key).commit()

    companion object {
        private const val SECURE_PREF_NAME = "securePreferences"
    }
}