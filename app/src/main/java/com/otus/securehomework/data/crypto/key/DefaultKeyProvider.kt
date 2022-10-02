package com.otus.securehomework.data.crypto.key

import android.os.Build
import android.security.keystore.KeyGenParameterSpec
import android.security.keystore.KeyProperties
import androidx.annotation.RequiresApi
import java.security.KeyStore
import javax.crypto.KeyGenerator
import javax.crypto.SecretKey

@RequiresApi(Build.VERSION_CODES.M)
class DefaultKeyProvider : KeyProvider {

    private val keyStore by lazy {
        KeyStore.getInstance(KEY_PROVIDER).apply {
            load(null)
        }
    }

    override fun getAesKey(): SecretKey {
        return keyStore.getKey(AES_KEY_ALIAS, null) as? SecretKey ?: generateAesKey()
    }

    private fun generateAesKey(): SecretKey {
        val keySpec = KeyGenParameterSpec.Builder(
            AES_KEY_ALIAS,
            KeyProperties.PURPOSE_ENCRYPT or KeyProperties.PURPOSE_DECRYPT
        )
            .setBlockModes(KeyProperties.BLOCK_MODE_CBC)
            .setEncryptionPaddings(KeyProperties.ENCRYPTION_PADDING_PKCS7)
            .setRandomizedEncryptionRequired(false)
            .build()

        return KeyGenerator.getInstance(AES_ALGORITHM, KEY_PROVIDER).apply {
            init(keySpec)
        }.generateKey()
    }

    private companion object {
        const val KEY_PROVIDER = "AndroidKeyStore"
        const val AES_ALGORITHM = "AES"
        const val AES_KEY_ALIAS = "AES_KEY"
    }
}
