package com.otus.securehomework.data.keymanager

import javax.crypto.SecretKey

interface SecretKeyManager {
    fun getSecretKey(): SecretKey
}
