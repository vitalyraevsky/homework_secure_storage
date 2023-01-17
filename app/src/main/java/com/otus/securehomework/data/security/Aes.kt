package com.otus.securehomework.data.security

import java.security.Key
import javax.inject.Inject

interface Aes {
    fun encrypt(plainText: CharSequence, key: Key): String

    fun decrypt(encrypted: CharSequence, key: Key): CharSequence
}