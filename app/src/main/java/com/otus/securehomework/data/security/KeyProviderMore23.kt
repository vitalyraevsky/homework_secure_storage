package com.otus.securehomework.data.security

import android.os.Build
import android.security.keystore.KeyGenParameterSpec
import android.security.keystore.KeyProperties
import androidx.annotation.RequiresApi
import java.security.KeyStore
import javax.crypto.KeyGenerator
import javax.crypto.SecretKey

@RequiresApi(Build.VERSION_CODES.M)
class KeyProviderMore23 : KeyProvider {

    private val keyStore by lazy {
        KeyStore.getInstance(KEY_PROVIDER).apply {
            load(null)
        }
    }

    override fun getAesSecretKey(): SecretKey =
        keyStore.getKey(AES_KEY_ALIAS, null) as? SecretKey ?: generateAesSecretKey()

    private fun generateAesSecretKey() : SecretKey {
        val generator = KeyGenerator.getInstance(AES_ALGORITHM, KEY_PROVIDER)
        val spec = KeyGenParameterSpec.Builder(
            AES_KEY_ALIAS,
            KeyProperties.PURPOSE_ENCRYPT or KeyProperties.PURPOSE_DECRYPT
        )
            .setBlockModes(KeyProperties.BLOCK_MODE_CBC)
            .setEncryptionPaddings(KeyProperties.ENCRYPTION_PADDING_PKCS7)
            .setRandomizedEncryptionRequired(false)
            .build()
        generator.init(spec)
        return generator.generateKey()
    }

    companion object{
        private const val AES_ALGORITHM = "AES"
        private const val AES_KEY_ALIAS = "AES_OTUS_DEMO"
        private const val KEY_PROVIDER = "AndroidKeyStore"
    }
}