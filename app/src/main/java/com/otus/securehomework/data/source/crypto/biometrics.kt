package com.otus.securehomework.data.source.crypto

import androidx.biometric.BiometricPrompt
import androidx.biometric.BiometricPrompt.AuthenticationResult
import androidx.biometric.auth.*
import androidx.fragment.app.FragmentActivity
import kotlinx.coroutines.CancellableContinuation
import kotlinx.coroutines.suspendCancellableCoroutine
import kotlin.coroutines.resumeWithException


/**
 * Shows an authentication prompt to the user.
 *
 * @param host A wrapper for the component that will host the prompt.
 * @param crypto A cryptographic object to be associated with this authentication.
 *
 * @return [AuthenticationResult] for a successful authentication.
 *
 * @throws AuthPromptErrorException  when an unrecoverable error has been encountered and
 * authentication has stopped.
 * @throws AuthPromptFailureException when an authentication attempt by the user has been rejected.
 *
 * @see Class3BiometricAuthPrompt.authenticate(AuthPromptHost, AuthPromptCallback)
 *
 * @sample androidx.biometric.samples.auth.class3BiometricAuth
 */
suspend fun Class3BiometricAuthPrompt.auth(
    host: AuthPromptHost,
    crypto: BiometricPrompt.CryptoObject?,
): AuthenticationResult {
    return suspendCancellableCoroutine { continuation ->
        val authPrompt = startAuthentication(
            host,
            crypto,
            Runnable::run,
            CoroutineAuthPromptCallback(continuation)
        )

        continuation.invokeOnCancellation {
            authPrompt.cancelAuthentication()
        }
    }
}


/**
 * Shows an authentication prompt to the user.
 *
 * @param host A wrapper for the component that will host the prompt.
 *
 * @return [AuthenticationResult] for a successful authentication.
 *
 * @throws AuthPromptErrorException  when an unrecoverable error has been encountered and
 * authentication has stopped.
 * @throws AuthPromptFailureException when an authentication attempt by the user has been rejected.
 *
 * @see Class2BiometricAuthPrompt.authenticate(AuthPromptHost, AuthPromptCallback)
 *
 * @sample androidx.biometric.samples.auth.class2BiometricAuth
 */
suspend fun Class2BiometricAuthPrompt.auth(
    host: AuthPromptHost,
): AuthenticationResult {
    return suspendCancellableCoroutine { continuation ->
        val authPrompt = startAuthentication(
            host,
            Runnable::run,
            CoroutineAuthPromptCallback(continuation)
        )

        continuation.invokeOnCancellation {
            authPrompt.cancelAuthentication()
        }
    }
}


/**
 * Implementation of [AuthPromptCallback] used to transform callback results for coroutine APIs.
 */
internal class CoroutineAuthPromptCallback(
    private val continuation: CancellableContinuation<AuthenticationResult>
) : AuthPromptCallback() {
    override fun onAuthenticationError(
        activity: FragmentActivity?,
        errorCode: Int,
        errString: CharSequence
    ) {
        continuation.resumeWithException(AuthPromptErrorException(errorCode, errString))
    }

    override fun onAuthenticationSucceeded(
        activity: FragmentActivity?,
        result: AuthenticationResult
    ) {
        continuation.resumeWith(Result.success(result))
    }

    override fun onAuthenticationFailed(activity: FragmentActivity?) {
    }
}