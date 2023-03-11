package com.otus.securehomework.data.biometric

import androidx.biometric.BiometricPrompt
import androidx.biometric.auth.AuthPromptErrorException
import kotlinx.coroutines.CancellableContinuation
import kotlin.coroutines.resumeWithException

internal class CoroutineBiometricPromptCallback(
    private val continuation: CancellableContinuation<BiometricPrompt.AuthenticationResult>
) : BiometricPrompt.AuthenticationCallback() {
    override fun onAuthenticationError(errorCode: Int, errString: CharSequence) {
        continuation.resumeWithException(AuthPromptErrorException(errorCode, errString))
    }

    override fun onAuthenticationSucceeded(result: BiometricPrompt.AuthenticationResult) {
        continuation.resumeWith(Result.success(result))
    }

    override fun onAuthenticationFailed() {
        //stub
    }
}