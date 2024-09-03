package com.otus.securehomework.data.encryptor.secretkeymanager

import javax.crypto.SecretKey

interface SecretKeyManager {
    suspend fun getSecretKey(keyName: String): SecretKey
}