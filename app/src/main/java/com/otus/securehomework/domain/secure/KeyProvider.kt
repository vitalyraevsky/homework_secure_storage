package com.otus.securehomework.domain.secure

import java.security.KeyStore
import javax.crypto.SecretKey

abstract class KeyProvider {

    protected val keyStore: KeyStore by lazy {
        KeyStore.getInstance(KEY_PROVIDER).apply {
            load(null)
        }
    }

    abstract val secretKey: SecretKey

    companion object {
        const val KEY_PROVIDER = "AndroidKeyStore"
    }
}