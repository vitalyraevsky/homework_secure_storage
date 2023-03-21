package com.otus.securehomework.data.security

import java.security.Key

interface IEncryptorDecryptor {

    fun encryptAes(plainText: CharSequence, key: Key): String

    fun decryptAes(encrypted: CharSequence, key: Key): CharSequence
}