package com.otus.securehomework.data.repository

import com.otus.securehomework.data.crypto.Keys
import com.otus.securehomework.data.crypto.Security
import javax.inject.Inject

class SecurityRepository @Inject constructor(
    private val keys: Keys,
    private val security: Security
) {
    fun encryptAes(s: String): String{
       return security.encryptAes(s, keys.getAesSecretKey())
    }
    fun decryptAes(s: String): String{
        return security.decryptAes(s, keys.getAesSecretKey())
    }
}