package com.otus.securehomework.data.security

import java.security.Key

interface IEncryptorDecryptor {

    fun encryptAes(plainText: String, key: Key): String

    fun decryptAes(encrypted: String, key: Key): String
}