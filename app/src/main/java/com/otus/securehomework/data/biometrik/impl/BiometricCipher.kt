package com.otus.securehomework.data.biometrik.impl

import javax.crypto.Cipher

interface BiometricCipher {
    fun cipher(): Cipher
}