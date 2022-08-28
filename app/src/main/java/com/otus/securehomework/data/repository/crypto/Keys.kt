package com.otus.securehomework.data.repository.crypto

import android.os.Build
import android.security.keystore.KeyGenParameterSpec
import android.security.keystore.KeyProperties
import androidx.annotation.RequiresApi
import java.security.KeyStore
import javax.crypto.KeyGenerator
import javax.crypto.SecretKey

@RequiresApi(Build.VERSION_CODES.M)
class Keys : IKeyProvider {

    private val mKeyStore by lazy {
        KeyStore.getInstance(ANDROID_KEY_STORE).apply {
            load(null)
        }
    }

    override fun getAesSecretKey(): SecretKey {
        return mKeyStore.getKey(AES_KEY_ALIAS, null) as? SecretKey ?: generateAesSecretKey()
    }

    private fun generateAesSecretKey() =
        KeyGenerator.getInstance(AES_ALGORITHM, ANDROID_KEY_STORE).apply {
            init(getKeyGenSpec())
        }.generateKey()

    private fun getKeyGenSpec(): KeyGenParameterSpec =
        KeyGenParameterSpec.Builder(
            AES_KEY_ALIAS,
            KeyProperties.PURPOSE_ENCRYPT or KeyProperties.PURPOSE_DECRYPT
        )
            .setBlockModes(KeyProperties.BLOCK_MODE_CBC)
            .setEncryptionPaddings(KeyProperties.ENCRYPTION_PADDING_PKCS7)
            .setRandomizedEncryptionRequired(false)
            .build()

    companion object {
        const val ANDROID_KEY_STORE = "AndroidKeyStore"
        private const val AES_KEY_ALIAS = "AES_DEMO"
        private const val AES_ALGORITHM = "AES"
    }

}