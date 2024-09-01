package com.otus.securehomework.domain.security

import java.security.KeyStore
import javax.crypto.SecretKey

internal const val KEY_PROVIDER = "AndroidKeyStore"

interface KeyProvider {

    val secretKey: SecretKey

    companion object {
        val keyStore: KeyStore by lazy {
            KeyStore.getInstance(KEY_PROVIDER).apply {
                load(null)
            }
        }
    }
}
