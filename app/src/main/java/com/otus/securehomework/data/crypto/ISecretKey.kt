package com.otus.securehomework.data.crypto

import javax.crypto.SecretKey

interface ISecretKey {
    fun getSecretKey(): SecretKey

    companion object {
        const val ANDROID_KEY_STORE = "AndroidKeyStore"
    }
}
