package com.otus.securehomework.domain.security

import android.os.Build
import android.security.keystore.KeyGenParameterSpec
import android.security.keystore.KeyProperties
import androidx.annotation.RequiresApi
import javax.crypto.KeyGenerator
import javax.crypto.SecretKey

private const val AES_KEY_ALIAS = "AES_OTUS_ALIAS"

@RequiresApi(Build.VERSION_CODES.M)
class KeyProviderAboveMApiImpl : KeyProvider {

    override val secretKey: SecretKey
        get() = KeyProvider.keyStore.getKey(AES_KEY_ALIAS, null) as? SecretKey ?: generateKey()

    private fun generateKey(): SecretKey {
        val keyGenerator = KeyGenerator.getInstance(KeyProperties.KEY_ALGORITHM_AES, KEY_PROVIDER)
        val spec = KeyGenParameterSpec.Builder(
            AES_KEY_ALIAS,
            KeyProperties.PURPOSE_ENCRYPT or KeyProperties.PURPOSE_DECRYPT
        )
            .setBlockModes(KeyProperties.BLOCK_MODE_GCM)
            .setEncryptionPaddings(KeyProperties.ENCRYPTION_PADDING_NONE)
            .setRandomizedEncryptionRequired(false)
            .build()
        keyGenerator.init(spec)
        return keyGenerator.generateKey()
    }
}
