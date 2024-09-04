package com.otus.securehomework.data.protection

interface ITokenManager {
    fun encryptToken(token: String): String
    fun decryptToken(encryptedToken: String): String?
}