package com.otus.securehomework.data.crypto

import android.os.Build
import android.security.keystore.KeyGenParameterSpec
import android.security.keystore.KeyProperties
import androidx.annotation.RequiresApi
import java.security.KeyStore
import javax.crypto.KeyGenerator
import javax.crypto.SecretKey
import javax.inject.Inject

class KeyProviderPostM @Inject constructor(private val keyStore: KeyStore) : KeyProvider() {

    @RequiresApi(Build.VERSION_CODES.M)
    override fun getAesSecretKey(): SecretKey {
        return keyStore.getKey(AES_KEY_ALIAS, null) as? SecretKey ?: generateAesSecretKey()
    }

    @RequiresApi(Build.VERSION_CODES.M)
    override fun generateAesSecretKey(): SecretKey {
        return getKeyGenerator().generateKey()
    }


    @RequiresApi(Build.VERSION_CODES.M)
    private fun getKeyGenerator() = KeyGenerator.getInstance(AES_ALGORITHM, KEY_PROVIDER).apply {
        init(getKeyGenSpec())
    }

    @RequiresApi(Build.VERSION_CODES.M)
    private fun getKeyGenSpec(): KeyGenParameterSpec {
        return KeyGenParameterSpec.Builder(
            AES_KEY_ALIAS,
            KeyProperties.PURPOSE_ENCRYPT or KeyProperties.PURPOSE_DECRYPT
        )
            .setBlockModes(KeyProperties.BLOCK_MODE_GCM)
            .setEncryptionPaddings(KeyProperties.ENCRYPTION_PADDING_NONE)
            .setUserAuthenticationRequired(false)
            .setRandomizedEncryptionRequired(false)
            .setKeySize(KEY_LENGTH)
            .build()
    }
}