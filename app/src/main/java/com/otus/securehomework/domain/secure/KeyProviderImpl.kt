package com.otus.securehomework.domain.secure

import android.content.Context
import android.os.Build
import android.security.keystore.KeyGenParameterSpec
import android.security.keystore.KeyProperties
import androidx.annotation.RequiresApi
import dagger.hilt.android.qualifiers.ApplicationContext
import javax.crypto.KeyGenerator
import javax.crypto.SecretKey

@RequiresApi(Build.VERSION_CODES.M)
class KeyProviderImpl(
    @ApplicationContext val context: Context
) : KeyProvider() {
    override val secretKey: SecretKey
        get() = keyStore.getKey(AES_KEY_ALIAS, null) as? SecretKey
            ?: keyGenerator.generateKey()

    private val keyGenerator by lazy {
        KeyGenerator.getInstance(AES_ALGORITHM, KEY_PROVIDER).apply {
            init(
                KeyGenParameterSpec.Builder(
                    /* keystoreAlias = */ AES_KEY_ALIAS,
                    /* purposes = */ KeyProperties.PURPOSE_ENCRYPT or KeyProperties.PURPOSE_DECRYPT
                )
                    .setBlockModes(KeyProperties.BLOCK_MODE_GCM)
                    .setEncryptionPaddings(KeyProperties.ENCRYPTION_PADDING_NONE)
                    .setRandomizedEncryptionRequired(false)
                    .setKeySize(KEY_LENGTH)
                    .build()
            )
        }
    }

    private companion object {
        const val AES_KEY_ALIAS = "AES_DEMO"
        const val AES_ALGORITHM = "AES"
        const val KEY_LENGTH = 256
    }
}
