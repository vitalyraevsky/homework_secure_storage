package com.otus.securehomework.security

import android.annotation.SuppressLint
import android.content.Context
import android.content.pm.PackageManager
import android.os.Build
import android.security.keystore.KeyGenParameterSpec
import android.security.keystore.KeyProperties
import androidx.biometric.BiometricPrompt
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import java.security.KeyStore
import javax.crypto.Cipher
import javax.crypto.KeyGenerator
import javax.crypto.SecretKey
import javax.inject.Inject

class BiometricHelper @Inject constructor(@ApplicationContext private val applicationContext: Context) {
    private val keyAlias by lazy { "${applicationContext.packageName}.biometricKey" }

    val isBiometricAuthEnabled: Flow<Boolean>
        get() = applicationContext.biometricStore.data.map { prefs ->
            prefs[BIOMETRIC_AUTH_ENABLED_KEY]?.let { it == BIOMETRIC_AUTH_ENABLED } ?: true
        }

    suspend fun enableBiometricAuth(enable: Boolean) {
        applicationContext.biometricStore.edit { prefs ->
            prefs[BIOMETRIC_AUTH_ENABLED_KEY] =
                if (enable) BIOMETRIC_AUTH_ENABLED else BIOMETRIC_AUTH_DISABLED
        }
    }

    fun getEncryptor(): BiometricPrompt.CryptoObject {
        val encryptor = Cipher.getInstance("AES/GCM/NoPadding").apply {
            init(Cipher.ENCRYPT_MODE, getOrCreateKey())
        }

        return BiometricPrompt.CryptoObject(encryptor)
    }

    @SuppressLint("NewApi")
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

                    val hasStringBox = applicationContext
                        .packageManager
                        .hasSystemFeature(PackageManager.FEATURE_STRONGBOX_KEYSTORE)

                    setIsStrongBoxBacked(hasStringBox)
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

    companion object {
        private val Context.biometricStore by preferencesDataStore(name = "biometricPref")
        private const val KEYSTORE_PROVIDER = "AndroidKeyStore"
        private const val KEY_SIZE = 256
        private val BIOMETRIC_AUTH_ENABLED_KEY = stringPreferencesKey("biometricAuthEnabled")
        private const val BIOMETRIC_AUTH_ENABLED = "1"
        private const val BIOMETRIC_AUTH_DISABLED = "0"
    }
}