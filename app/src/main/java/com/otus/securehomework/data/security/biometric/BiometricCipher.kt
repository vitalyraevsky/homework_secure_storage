package com.otus.securehomework.data.security.biometric

import android.content.Context
import android.content.pm.PackageManager
import android.os.Build
import android.security.keystore.KeyGenParameterSpec
import android.security.keystore.KeyProperties.AUTH_BIOMETRIC_STRONG
import android.security.keystore.KeyProperties.BLOCK_MODE_GCM
import android.security.keystore.KeyProperties.ENCRYPTION_PADDING_NONE
import android.security.keystore.KeyProperties.KEY_ALGORITHM_AES
import android.security.keystore.KeyProperties.PURPOSE_DECRYPT
import android.security.keystore.KeyProperties.PURPOSE_ENCRYPT
import androidx.annotation.RequiresApi
import androidx.biometric.BiometricPrompt
import dagger.hilt.android.qualifiers.ApplicationContext
import java.security.KeyStore
import javax.crypto.Cipher
import javax.crypto.KeyGenerator
import javax.crypto.SecretKey
import javax.inject.Inject

private const val KEYSTORE_PROVIDER = "AndroidKeyStore"
private const val KEY_SIZE = 256

private const val TRANSFORMATION = "$KEY_ALGORITHM_AES/$BLOCK_MODE_GCM/$ENCRYPTION_PADDING_NONE"

@RequiresApi(Build.VERSION_CODES.M)
class BiometricCipher @Inject constructor(
    @ApplicationContext private val context: Context
) {
    private val keyAlias by lazy { "${context.packageName}.biometricKey" }

    fun getEncryptor(): BiometricPrompt.CryptoObject {
        val encryptor = Cipher.getInstance(TRANSFORMATION).apply {
            init(Cipher.ENCRYPT_MODE, getOrCreateKey())
        }

        return BiometricPrompt.CryptoObject(encryptor)
    }

    private fun getOrCreateKey(): SecretKey {
        val keystore = KeyStore.getInstance(KEYSTORE_PROVIDER).apply {
            load(null)
        }

        keystore.getKey(keyAlias, null)?.let { key ->
            return key as SecretKey
        }

        val keySpec = KeyGenParameterSpec.Builder(keyAlias, PURPOSE_ENCRYPT or PURPOSE_DECRYPT)
            .setBlockModes(BLOCK_MODE_GCM)
            .setEncryptionPaddings(ENCRYPTION_PADDING_NONE)
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
                    setUserAuthenticationParameters(0, AUTH_BIOMETRIC_STRONG)
                }
            }.build()

        val keyGenerator = KeyGenerator.getInstance(KEY_ALGORITHM_AES, KEYSTORE_PROVIDER).apply {
            init(keySpec)
        }

        return keyGenerator.generateKey()
    }
}