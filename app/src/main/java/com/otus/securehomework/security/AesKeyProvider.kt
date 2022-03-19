package com.otus.securehomework.security

import javax.crypto.SecretKey

interface AesKeyProvider {
    fun getKey() : SecretKey
}