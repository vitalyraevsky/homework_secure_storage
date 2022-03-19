package com.otus.securehomework.security

import androidx.biometric.BiometricPrompt

interface EncryptionService {
    fun encrypt(plainText: String): String

    fun decrypt(encrypted: String): String

    fun getBiometricCryptoObject(): BiometricPrompt.CryptoObject
}