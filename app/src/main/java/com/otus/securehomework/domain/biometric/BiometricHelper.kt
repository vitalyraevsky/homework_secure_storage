package com.otus.securehomework.domain.biometric

import android.annotation.SuppressLint
import android.content.Context
import android.content.pm.PackageManager
import android.os.Build
import android.security.keystore.KeyGenParameterSpec
import android.security.keystore.KeyProperties
import androidx.biometric.BiometricManager
import androidx.biometric.BiometricManager.Authenticators.BIOMETRIC_STRONG
import androidx.biometric.BiometricManager.Authenticators.BIOMETRIC_WEAK
import androidx.biometric.BiometricManager.BIOMETRIC_SUCCESS
import androidx.biometric.BiometricPrompt
import androidx.biometric.auth.Class2BiometricAuthPrompt
import androidx.biometric.auth.Class3BiometricAuthPrompt
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import com.otus.securehomework.R
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import java.security.KeyStore
import javax.crypto.Cipher
import javax.crypto.KeyGenerator
import javax.crypto.SecretKey

class BiometricHelper(@ApplicationContext private val context: Context) {

    private val keyAlias by lazy { "${context.packageName}.biometricKey" }
    private val Context.biometricStore by preferencesDataStore(name = "biometricPref")
    private val biometricManager = BiometricManager.from(context)
    private val resources by lazy { context.resources }

    val isAuthEnabled: Flow<Boolean>
        get() = context.biometricStore.data.map { prefs ->
            prefs[BIOMETRIC_AUTH_ENABLED_KEY]?.let { it == BIOMETRIC_AUTH_ENABLED } ?: true
        }

    val isBiometricStrongEnabled: Boolean
        get() = canAuthenticate(BIOMETRIC_STRONG)


    val isBiometricWeakEnabled: Boolean
        get() = canAuthenticate(BIOMETRIC_WEAK)

    val strongAuthPrompt = Class3BiometricAuthPrompt.Builder(
        resources.getText(R.string.biometric_title_strong),
        resources.getText(R.string.biometric_negative_button)
    ).apply {
        setSubtitle(resources.getText(R.string.biometric_subtitle))
        setDescription(resources.getText(R.string.biometric_description))
        setConfirmationRequired(true)
    }.build()

    val wealAuthPrompt = Class2BiometricAuthPrompt.Builder(
        resources.getText(R.string.biometric_title_weak),
        resources.getText(R.string.biometric_negative_button)
    ).apply {
        setSubtitle(resources.getText(R.string.biometric_subtitle))
        setDescription(resources.getText(R.string.biometric_description))
        setConfirmationRequired(true)
    }.build()

    suspend fun enableAuth(enable: Boolean) {
        context.biometricStore.edit { prefs ->
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

        val keySpec = KeyGenParameterSpec.Builder(
            keyAlias,
            KeyProperties.PURPOSE_ENCRYPT or KeyProperties.PURPOSE_DECRYPT
        )
            .setBlockModes(KeyProperties.BLOCK_MODE_GCM)
            .setEncryptionPaddings(KeyProperties.ENCRYPTION_PADDING_NONE)
            .setKeySize(KEY_SIZE)
            .setUserAuthenticationRequired(true)
            .apply {
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
                    setUnlockedDeviceRequired(true)

                    val hasStringBox = context
                        .packageManager
                        .hasSystemFeature(PackageManager.FEATURE_STRONGBOX_KEYSTORE)

                    setIsStrongBoxBacked(hasStringBox)
                }

                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
                    setUserAuthenticationParameters(0, KeyProperties.AUTH_BIOMETRIC_STRONG)
                }
            }.build()

        val keyGenerator =
            KeyGenerator.getInstance(KeyProperties.KEY_ALGORITHM_AES, KEYSTORE_PROVIDER).apply {
                init(keySpec)
            }

        return keyGenerator.generateKey()
    }

    private fun canAuthenticate(authenticator: Int) =
        biometricManager.canAuthenticate(authenticator) == BIOMETRIC_SUCCESS

    private   companion object {

         const val KEYSTORE_PROVIDER = "AndroidKeyStore"
         const val KEY_SIZE = 256
         val BIOMETRIC_AUTH_ENABLED_KEY = stringPreferencesKey("biometricAuthEnabled")
         const val BIOMETRIC_AUTH_ENABLED = "1"
         const val BIOMETRIC_AUTH_DISABLED = "0"
    }
}