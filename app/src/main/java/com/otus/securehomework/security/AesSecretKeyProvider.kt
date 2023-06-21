package com.otus.securehomework.security

import java.security.KeyStore
import javax.crypto.SecretKey

abstract class AesSecretKeyProvider {
    protected val keyStore by lazy {
        KeyStore.getInstance(KEY_PROVIDER).apply {
            load(null)
        }
    }

    abstract fun getAesSecretKey(): SecretKey

    companion object {
        const val KEY_PROVIDER = "AndroidKeyStore"
    }
}