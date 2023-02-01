package com.otus.securehomework.security.biometric

import androidx.biometric.BiometricPrompt

interface BiometricAuthenticator {
    suspend fun authenticate(): BiometricPrompt.AuthenticationResult
}