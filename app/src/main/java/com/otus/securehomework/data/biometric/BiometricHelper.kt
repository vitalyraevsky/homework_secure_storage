package com.otus.securehomework.data.biometric

import android.content.Context
import androidx.biometric.BiometricManager
import androidx.biometric.BiometricManager.Authenticators.BIOMETRIC_STRONG
import androidx.biometric.BiometricManager.Authenticators.BIOMETRIC_WEAK
import androidx.biometric.BiometricPrompt
import androidx.core.content.ContextCompat
import androidx.fragment.app.FragmentActivity
import kotlinx.coroutines.suspendCancellableCoroutine
import javax.inject.Inject

class BiometricHelper @Inject constructor(
    private val context: Context
) {
    private val authenticators = BIOMETRIC_STRONG or BIOMETRIC_WEAK

    private val biometricManager = BiometricManager.from(context)

    private val executor = ContextCompat.getMainExecutor(context)

    fun isBiometricReady() =
        biometricManager.canAuthenticate(authenticators) == BiometricManager.BIOMETRIC_SUCCESS

    suspend fun showBiometricPrompt(
        title: String,
        cryptoObject: BiometricPrompt.CryptoObject? = null,
        allowDeviceCredential: Boolean = false
    ): BiometricPrompt.AuthenticationResult {

        val promptInfo = BiometricPrompt.PromptInfo.Builder()
            .setTitle(title)
            .apply {
                if (allowDeviceCredential) {
                    setAllowedAuthenticators(authenticators)
                } else {
                    setNegativeButtonText("Cancel")
                }
            }.build()

        return suspendCancellableCoroutine { continuation ->
            BiometricPrompt(
                context as FragmentActivity,
                executor,
                CoroutineBiometricPromptCallback(continuation)
            ).apply {
                cryptoObject?.let {
                    authenticate(promptInfo, cryptoObject)
                } ?: authenticate(promptInfo)
            }
        }
    }
}
