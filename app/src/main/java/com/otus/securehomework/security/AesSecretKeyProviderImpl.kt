package com.otus.securehomework.security

import android.os.Build
import android.security.keystore.KeyGenParameterSpec
import android.security.keystore.KeyProperties
import androidx.annotation.RequiresApi
import javax.crypto.KeyGenerator
import javax.crypto.SecretKey

@RequiresApi(Build.VERSION_CODES.M)
class AesSecretKeyProviderImpl: AesSecretKeyProvider() {
    private val keyGenerator by lazy {
        KeyGenerator.getInstance(
            AES_ALGORITHM,
            KEY_PROVIDER
        ).apply {
            init(
                KeyGenParameterSpec.Builder(
                    AES_KEY_ALIAS,
                    KeyProperties.PURPOSE_ENCRYPT or KeyProperties.PURPOSE_DECRYPT
                )
                    .setRandomizedEncryptionRequired(false)
                    .setBlockModes(KeyProperties.BLOCK_MODE_GCM)
                    .setEncryptionPaddings(KeyProperties.ENCRYPTION_PADDING_NONE)
                    .build()
            )
        }
    }

    override fun getAesSecretKey() =
        keyStore.getKey(AES_KEY_ALIAS, null) as? SecretKey ?: keyGenerator.generateKey()

    companion object {
        private const val AES_KEY_ALIAS = "AES_DEMO"
        private const val AES_ALGORITHM = "AES"
    }
}