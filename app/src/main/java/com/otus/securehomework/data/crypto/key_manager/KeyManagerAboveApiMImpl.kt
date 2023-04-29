package com.otus.securehomework.data.crypto.key_manager

import android.os.Build
import android.security.keystore.KeyGenParameterSpec
import android.security.keystore.KeyProperties
import androidx.annotation.RequiresApi
import com.otus.securehomework.data.enums.KeyManagerType
import java.security.KeyStore
import javax.crypto.KeyGenerator
import javax.crypto.SecretKey
import javax.inject.Inject

@RequiresApi(Build.VERSION_CODES.M)
class KeyManagerAboveApiMImpl @Inject constructor() : KeyManager {

    override fun getSecretKey(
        keyName: String,
        type: KeyManagerType
    ): SecretKey {
        val keyStore = KeyStore.getInstance(ANDROID_KEY_STORE).apply { load(null) }
        if (keyStore.getEntry(keyName, null) != null) {
            val secretKeyEntry = keyStore.getEntry(keyName, null) as KeyStore.SecretKeyEntry
            return secretKeyEntry.secretKey ?: generateSecretKey(keyName, type)
        }
        return generateSecretKey(keyName, type)
    }

    private fun generateSecretKey(
        keyName: String,
        type: KeyManagerType
    ): SecretKey {
        val keyGenerator = KeyGenerator.getInstance(
            KeyProperties.KEY_ALGORITHM_AES,
            ANDROID_KEY_STORE
        )
        keyGenerator.init(getKeyGenParameterSpec(keyName, type))
        return keyGenerator.generateKey()
    }

    private fun getKeyGenParameterSpec(
        keyName: String,
        type: KeyManagerType
    ): KeyGenParameterSpec {
        return when (type) {
            KeyManagerType.ENCRYPTION -> getEncryptionKeyGenParameterSpec(keyName)
            KeyManagerType.BIOMETRIC -> getBiometricKeyGenParameterSpec(keyName)
        }
    }

    private fun getEncryptionKeyGenParameterSpec(keyName: String): KeyGenParameterSpec {
        val purposes = KeyProperties.PURPOSE_ENCRYPT or KeyProperties.PURPOSE_DECRYPT
        return KeyGenParameterSpec.Builder(keyName, purposes)
            .setBlockModes(KeyProperties.BLOCK_MODE_CBC)
            .setEncryptionPaddings(KeyProperties.ENCRYPTION_PADDING_PKCS7)
            .setRandomizedEncryptionRequired(false)
            .build()
    }

    private fun getBiometricKeyGenParameterSpec(keyName: String): KeyGenParameterSpec {
        val purposes = KeyProperties.PURPOSE_ENCRYPT or KeyProperties.PURPOSE_DECRYPT
        return KeyGenParameterSpec.Builder(keyName, purposes)
            .setBlockModes(KeyProperties.BLOCK_MODE_CBC)
            .setEncryptionPaddings(KeyProperties.ENCRYPTION_PADDING_PKCS7)
            .setUserAuthenticationRequired(true)
            .apply {
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
                    setUserAuthenticationParameters(0, KeyProperties.AUTH_BIOMETRIC_STRONG)
                }
            }
            .build()
    }

    companion object {
        private const val ANDROID_KEY_STORE = "AndroidKeyStore"
    }

}