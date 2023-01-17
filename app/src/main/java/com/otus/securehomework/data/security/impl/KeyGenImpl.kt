package com.otus.securehomework.data.security.impl

import android.os.Build
import android.security.keystore.KeyGenParameterSpec
import android.security.keystore.KeyProperties
import androidx.annotation.RequiresApi
import com.otus.securehomework.data.security.KeyGen
import java.security.KeyStore
import javax.crypto.KeyGenerator
import javax.crypto.SecretKey
import javax.inject.Inject

@RequiresApi(Build.VERSION_CODES.M)
class KeyGenImpl @Inject constructor(): KeyGen {

    companion object {
        private const val KEY_PROVIDER = "AndroidKeyStore"

        private const val AES_ALGORITHM = "AES"
        private const val AES_KEY_ALIAS = "AES_DEMO"
    }

    private val keyStore by lazy {
        KeyStore.getInstance(KEY_PROVIDER).apply {
            load(null)
        }
    }

    override fun generate(): SecretKey =
        keyStore.getKey(AES_KEY_ALIAS, null) as? SecretKey ?: generateAesSecretKey()

    private fun generateAesSecretKey() = getAesKeyGenerator().generateKey()

    private fun getAesKeyGenerator() = KeyGenerator.getInstance(AES_ALGORITHM, KEY_PROVIDER).apply {
        init(getAesKeyGenSpec())
    }
    private fun getAesKeyGenSpec(): KeyGenParameterSpec {
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