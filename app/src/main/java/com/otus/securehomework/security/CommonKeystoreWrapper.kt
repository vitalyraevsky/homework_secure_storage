package com.otus.securehomework.security

import java.security.KeyStore

abstract class BaseKeystoreWrapperImpl : KeystoreWrapper {

    protected val keyStore: KeyStore by lazy {
        KeyStore.getInstance(ANDROID_KEYSTORE).apply {
            load(null)
        }
    }

    override fun removeKeyStoreKey() {
        if (isKeyExists(keyStore)) {
            keyStore.deleteEntry(KEY_ALIAS)
        }
    }

    protected fun isKeyExists(keyStore: KeyStore): Boolean {
        val aliases = keyStore.aliases()
        while (aliases.hasMoreElements()) {
            return (KEY_ALIAS == aliases.nextElement())
        }
        return false
    }

    companion object {
        const val ANDROID_KEYSTORE = "AndroidKeyStore"
        const val KEY_ALIAS = "KeyAlias123"
    }
}
