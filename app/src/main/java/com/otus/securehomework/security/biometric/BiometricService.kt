package com.otus.securehomework.security.biometric

import androidx.biometric.auth.AuthPromptHost

interface BiometricService {
    val isAvailable: Boolean

    suspend fun authorize(host: AuthPromptHost): Boolean
}

