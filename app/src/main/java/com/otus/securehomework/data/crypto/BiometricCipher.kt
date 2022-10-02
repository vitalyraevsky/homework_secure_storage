package com.otus.securehomework.data.crypto

import android.content.Context
import android.content.pm.PackageManager
import androidx.biometric.BiometricPrompt
import android.os.Build
import android.security.keystore.KeyGenParameterSpec
import android.security.keystore.KeyProperties.*
import androidx.annotation.RequiresApi
import dagger.hilt.android.qualifiers.ApplicationContext
import java.security.KeyStore
import javax.crypto.Cipher
import javax.crypto.KeyGenerator
import javax.crypto.SecretKey
import javax.inject.Inject

@RequiresApi(Build.VERSION_CODES.P)
class BiometricCipher @Inject constructor(
    @ApplicationContext
    private val applicationContext: Context
) {
    private val keyAlias get() = "${applicationContext.packageName}.biometricKey"

    private val keyStore by lazy {
        KeyStore.getInstance(KEY_PROVIDER).apply {
            load(null)
        }
    }

    fun getEncryptor(): BiometricPrompt.CryptoObject {
        val encryptor = Cipher.getInstance(TRANSFORMATION).apply {
            init(Cipher.ENCRYPT_MODE, getOrCreateKey())
        }

        return BiometricPrompt.CryptoObject(encryptor)
    }

    private fun getOrCreateKey(): SecretKey {
        return keyStore.getKey(keyAlias, null) as? SecretKey ?: generateKey()
    }

    private fun generateKey(): SecretKey {
        val keySpec = KeyGenParameterSpec.Builder(keyAlias, PURPOSE_ENCRYPT or PURPOSE_DECRYPT)
            .setBlockModes(BLOCK_MODE_GCM)
            .setEncryptionPaddings(ENCRYPTION_PADDING_NONE)
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
                    setUserAuthenticationParameters(0, AUTH_BIOMETRIC_STRONG)
                }
            }.build()

        val keyGenerator = KeyGenerator.getInstance(KEY_ALGORITHM_AES, KEY_PROVIDER).apply {
            init(keySpec)
        }

        return keyGenerator.generateKey()
    }

    private companion object {
        const val KEY_PROVIDER = "AndroidKeyStore"
        const val KEY_SIZE = 256

        const val TRANSFORMATION =
            "$KEY_ALGORITHM_AES/$BLOCK_MODE_GCM/$ENCRYPTION_PADDING_NONE"
    }
}