package com.otus.securehomework.data.crypto

import android.os.Build
import android.security.keystore.KeyGenParameterSpec
import android.security.keystore.KeyProperties
import android.util.Base64
import android.util.Log
import androidx.annotation.RequiresApi
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import java.security.KeyStore
import javax.crypto.Cipher
import javax.crypto.KeyGenerator
import javax.crypto.SecretKey
import javax.crypto.spec.IvParameterSpec

class PostMTokenStorageImpl(
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
                .setUserAuthenticationRequired(true)
                .setRandomizedEncryptionRequired(false)
                .build()
            )

        return generator.generateKey()
    }

    @RequiresApi(Build.VERSION_CODES.M)
    private fun getSecretKey(): SecretKey {
        return keyStore.getKey(keyAlias, null) as? SecretKey ?: generateSecretKey()
    }

    private val iv = byteArrayOf(0x48, 0x13, 0x48, 0x48, 0x22, 0x48, 0xa8.toByte(), 0x21, 0x48, 0x0f, 0x48, 0x48, 0x18, 0x48,
        0xff.toByte(), 0x48)

    @RequiresApi(Build.VERSION_CODES.M)
    private fun encryptAes(plainText: String): String {

        val ivParams = IvParameterSpec(iv)

        val cipher = Cipher.getInstance("AES/CBC/PKCS5Padding").apply {
            init(Cipher.ENCRYPT_MODE, getSecretKey(), ivParams)
        }
        val encodedBytes = cipher.doFinal(plainText.toByteArray())
        return Base64.encodeToString(encodedBytes, Base64.DEFAULT)
    }

    @RequiresApi(Build.VERSION_CODES.M)
    private fun decryptAes(encrypted: String?): String {
        encrypted ?: return ""

        val ivParams = IvParameterSpec(iv)

        Log.d("Secret key", getSecretKey().toString())

        val cipher = Cipher.getInstance("AES/CBC/PKCS5Padding").apply {
            init(Cipher.ENCRYPT_MODE, getSecretKey(), ivParams)
        }
        val decodedBytes = Base64.decode(encrypted.toByteArray(), Base64.DEFAULT)
        val decoded = cipher.doFinal(decodedBytes)
        return String(decoded, Charsets.UTF_8)
    }

    @RequiresApi(Build.VERSION_CODES.M)
    override suspend fun saveAccessToken(token: String?) {
        dataStore.edit { preferences ->
            token?.let { preferences[stringPreferencesKey("key_access_token")] = encryptAes(it) }
        }
    }

    @RequiresApi(Build.VERSION_CODES.M)
    override suspend fun saveRefreshToken(token: String?) {
        dataStore.edit { preferences ->
            token?.let { preferences[stringPreferencesKey("key_refresh_token")] = encryptAes(it) }
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

}