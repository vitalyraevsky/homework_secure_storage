package com.otus.securehomework.data.crypto

import android.content.Context
import android.os.Build
import android.security.keystore.KeyGenParameterSpec
import android.security.keystore.KeyProperties
import androidx.annotation.RequiresApi
import java.security.KeyStore
import javax.crypto.KeyGenerator
import javax.crypto.SecretKey

private const val KEY_PROVIDER = "AndroidKeyStore"

private const val AES_ALGORITHM = "AES"
private const val AES_KEY_ALIAS = "AES_DEMO"

@RequiresApi(Build.VERSION_CODES.M)
class AesKeyGenerator() : AppKeyGenerator {

    private val keyStore by lazy {
        KeyStore.getInstance(KEY_PROVIDER).apply {
            load(null)
        }
    }

    override fun getSecretKey(): SecretKey {
        return keyStore.getKey(AES_KEY_ALIAS, null) as? SecretKey ?: generateSecretKey()
    }

    private fun generateSecretKey(): SecretKey {
        return KeyGenerator.getInstance(AES_ALGORITHM, KEY_PROVIDER).apply {
            init(getKeyGenSpec())
        }.generateKey()
    }

    private fun getKeyGenSpec(): KeyGenParameterSpec {
        return KeyGenParameterSpec.Builder(
            AES_KEY_ALIAS,
            KeyProperties.PURPOSE_ENCRYPT or KeyProperties.PURPOSE_DECRYPT
        )
            .setBlockModes(KeyProperties.BLOCK_MODE_CBC)
            .setEncryptionPaddings(KeyProperties.ENCRYPTION_PADDING_PKCS7)
            .setRandomizedEncryptionRequired(false)
            .build()
    }

}