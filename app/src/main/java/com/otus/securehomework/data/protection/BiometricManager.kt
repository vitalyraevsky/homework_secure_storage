package com.otus.securehomework.data.protection

import android.content.Context
import android.content.pm.PackageManager
import android.os.Build
import android.security.keystore.KeyGenParameterSpec
import android.security.keystore.KeyProperties
import androidx.annotation.RequiresApi
import androidx.biometric.BiometricPrompt
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import androidx.fragment.app.FragmentActivity
import com.otus.securehomework.data.dto.EncryptedEntity
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.firstOrNull
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.runBlocking
import java.security.KeyStore
import javax.crypto.Cipher
import javax.crypto.KeyGenerator
import javax.crypto.SecretKey
import javax.crypto.spec.GCMParameterSpec

private const val KEYSTORE_PROVIDER = "AndroidKeyStore"
private const val AUTH_TAG_SIZE = 128
private const val KEY_SIZE = 256
private const val TRANSFORMATION = "${KeyProperties.KEY_ALGORITHM_AES}/${KeyProperties.BLOCK_MODE_GCM}/${KeyProperties.ENCRYPTION_PADDING_NONE}"
private val Context.dataStore by preferencesDataStore(name = "biometric_prefs")

class BiometricManager(
    private val context: Context
) {

    private val keyAlias by lazy { "${context.packageName}.biometricKey" }
    private val biometricEnabledKey = booleanPreferencesKey("biometric_enabled")
    private val encryptedDataKey = stringPreferencesKey("encrypted_data")

    @RequiresApi(Build.VERSION_CODES.M)
    private fun getOrCreateKey(): SecretKey {
        val keystore = KeyStore.getInstance(KEYSTORE_PROVIDER).apply {
            load(null)
        }

        keystore.getKey(keyAlias, null)?.let { key ->
            return key as SecretKey
        }

        val keySpec = KeyGenParameterSpec.Builder(keyAlias, KeyProperties.PURPOSE_ENCRYPT or KeyProperties.PURPOSE_DECRYPT)
            .setBlockModes(KeyProperties.BLOCK_MODE_GCM)
            .setEncryptionPaddings(KeyProperties.ENCRYPTION_PADDING_NONE)
            .setKeySize(KEY_SIZE)
            .setUserAuthenticationRequired(true)
            .apply {
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
                    setUnlockedDeviceRequired(true)

                    val hasStrongbox = context.packageManager.hasSystemFeature(PackageManager.FEATURE_STRONGBOX_KEYSTORE)
                    setIsStrongBoxBacked(hasStrongbox)
                }

                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
                    setUserAuthenticationParameters(0, KeyProperties.AUTH_BIOMETRIC_STRONG)
                }
            }.build()

        val keyGenerator = KeyGenerator.getInstance(KeyProperties.KEY_ALGORITHM_AES, KEYSTORE_PROVIDER).apply {
            init(keySpec)
        }

        return keyGenerator.generateKey()
    }

    @RequiresApi(Build.VERSION_CODES.M)
    fun getEncryptor(): BiometricPrompt.CryptoObject {
        val encryptor = Cipher.getInstance(TRANSFORMATION).apply {
            init(Cipher.ENCRYPT_MODE, getOrCreateKey())
        }
        return BiometricPrompt.CryptoObject(encryptor)
    }

    @RequiresApi(Build.VERSION_CODES.M)
    fun getDecryptor(iv: ByteArray): BiometricPrompt.CryptoObject {
        // Ensure IV is of valid length
        if (iv.size != 12) {
            throw IllegalArgumentException("Invalid IV length: ${iv.size} bytes. Expected 12 bytes.")
        }

        val decryptor = Cipher.getInstance(TRANSFORMATION).apply {
            init(Cipher.DECRYPT_MODE, getOrCreateKey(), GCMParameterSpec(AUTH_TAG_SIZE, iv))
        }
        return BiometricPrompt.CryptoObject(decryptor)
    }

    fun encrypt(plaintext: String, encryptor: Cipher): EncryptedEntity {
        val ciphertext = encryptor.doFinal(plaintext.toByteArray())
        return EncryptedEntity(ciphertext, encryptor.iv)
    }

    fun decrypt(ciphertext: ByteArray, decryptor: Cipher): String {
        val plaintext = decryptor.doFinal(ciphertext)
        return String(plaintext, Charsets.UTF_8)
    }

    suspend fun setBiometricEnabled(enabled: Boolean) {
        context.dataStore.edit { preferences ->
            preferences[biometricEnabledKey] = enabled
        }
    }

    val isBiometricEnabled: Flow<Boolean>
        get() = context.dataStore.data.map { preferences ->
            preferences[biometricEnabledKey] ?: false
        }

    suspend fun saveEncryptedData(data: String, encryptor: Cipher) {
        val encryptedEntity = encrypt(data, encryptor)
        context.dataStore.edit { preferences ->
            preferences[encryptedDataKey] = "${encryptedEntity.iv.toBase64String()}|${encryptedEntity.ciphertext.toBase64String()}"
        }
    }

    fun getEncryptedData(decryptor: Cipher): String? {
        val data = runBlocking {
            context.dataStore.data.map { preferences ->
                preferences[encryptedDataKey]
            }.firstOrNull()
        }

        return data?.split("|")?.let {
            val iv = it[0].fromBase64()
            val ciphertext = it[1].fromBase64()
            // Validate that IV is correctly extracted
            if (iv.size != 12) {
                throw IllegalArgumentException("Invalid IV length after extraction: ${iv.size} bytes. Expected 12 bytes.")
            }
            decrypt(ciphertext, decryptor)
        }
    }

    @RequiresApi(Build.VERSION_CODES.P)
    fun setupBiometricPrompt(activity: FragmentActivity, callback: BiometricPrompt.AuthenticationCallback): BiometricPrompt {
        val promptInfo = BiometricPrompt.PromptInfo.Builder()
            .setTitle("Biometric Authentication")
            .setSubtitle("Authenticate using your biometric credential")
            .setNegativeButtonText("Cancel")
            .build()

        return BiometricPrompt(activity, activity.mainExecutor, callback).apply {
            authenticate(promptInfo)
        }
    }

    private fun ByteArray.toBase64String(): String = android.util.Base64.encodeToString(this, android.util.Base64.DEFAULT)
    private fun String.fromBase64(): ByteArray = android.util.Base64.decode(this, android.util.Base64.DEFAULT)
}
