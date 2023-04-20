package com.otus.securehomework.data.crypto

import android.annotation.SuppressLint
import android.content.Context
import android.os.Build
import android.provider.Settings
import android.security.keystore.KeyGenParameterSpec
import android.security.keystore.KeyProperties
import android.util.Base64
import androidx.annotation.RequiresApi
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import java.nio.charset.Charset
import java.security.KeyStore
import javax.crypto.Cipher
import javax.crypto.KeyGenerator
import javax.crypto.SecretKey
import javax.crypto.spec.IvParameterSpec

class PostMTokenStorageImpl(
    private val context: Context,
    private val dataStore: DataStore<Preferences>
): SecuredTokenStorage {

    private val keyProvider = "AndroidKeyStore"
    private val keyAlias = "postMKeyAlias"

    private val keyStore by lazy {
        KeyStore.getInstance(keyProvider).apply {
            load(null)
        }
    }

    @RequiresApi(Build.VERSION_CODES.M)
    private fun generateSecretKey(): SecretKey {
        val generator = KeyGenerator.getInstance("AES", keyProvider)
        generator.init(
            KeyGenParameterSpec.Builder(keyAlias, KeyProperties.PURPOSE_ENCRYPT or KeyProperties.PURPOSE_DECRYPT)
                .setBlockModes(KeyProperties.BLOCK_MODE_CBC)
                .setEncryptionPaddings(KeyProperties.ENCRYPTION_PADDING_PKCS7)
                .setRandomizedEncryptionRequired(false)
                .build()
            )

        return generator.generateKey()
    }

    @RequiresApi(Build.VERSION_CODES.M)
    private fun getSecretKey(): SecretKey {
        return keyStore.getKey(keyAlias, null) as? SecretKey ?: generateSecretKey()
    }

    @RequiresApi(Build.VERSION_CODES.M)
    private fun encryptAes(plainText: String): String {
        val cipher = Cipher.getInstance("AES/CBC/PKCS7Padding")
        val ivParams = IvParameterSpec(iv())
        cipher.init(Cipher.ENCRYPT_MODE, getSecretKey(), ivParams)
        val encodedBytes = cipher.doFinal(plainText.toByteArray(Charset.forName("UTF-8")))
        return Base64.encodeToString(encodedBytes, Base64.NO_WRAP)
    }

    @RequiresApi(Build.VERSION_CODES.M)
    private fun decryptAes(encrypted: String?): String? {
        encrypted ?: return null
        val cipher = Cipher.getInstance("AES/CBC/PKCS7Padding")
        val ivParams = IvParameterSpec(iv())
        cipher.init(Cipher.DECRYPT_MODE, getSecretKey(), ivParams)
        val base64Decoded = Base64.decode(encrypted, Base64.NO_WRAP)
        return String(cipher.doFinal(base64Decoded), Charset.forName("UTF-8"))
    }

    @RequiresApi(Build.VERSION_CODES.M)
    override suspend fun saveAccessToken(token: String?) {
        dataStore.edit { preferences ->
            token?.let {
                preferences[stringPreferencesKey("key_access_token")] = encryptAes(it)
            }
        }
    }

    @RequiresApi(Build.VERSION_CODES.M)
    override suspend fun saveRefreshToken(token: String?) {
        dataStore.edit { preferences ->
            token?.let {
                preferences[stringPreferencesKey("key_refresh_token")] = encryptAes(it)
            }
        }
    }

    @RequiresApi(Build.VERSION_CODES.M)
    override fun getAccessToken(): Flow<String?> {
        return dataStore.data.map { preferences ->
            decryptAes(preferences[stringPreferencesKey("key_access_token")])
        }
    }

    @RequiresApi(Build.VERSION_CODES.M)
    override fun getRefreshToken(): Flow<String?> {
        return dataStore.data.map { preferences ->
            decryptAes(preferences[stringPreferencesKey("key_refresh_token")])
        }
    }

    @SuppressLint("HardwareIds")
    private fun iv(): ByteArray {
        val id = Settings.Secure.getString(context.contentResolver, Settings.Secure.ANDROID_ID)
        return id.toByteArray(Charset.forName("UTF-8"))
    }

}