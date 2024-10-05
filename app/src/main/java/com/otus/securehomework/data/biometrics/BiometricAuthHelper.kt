package com.otus.securehomework.data.biometrics

import android.content.Context
import android.os.Build
import android.util.Log
import androidx.biometric.BiometricManager
import androidx.biometric.BiometricManager.Authenticators.BIOMETRIC_STRONG
import androidx.biometric.BiometricManager.Authenticators.BIOMETRIC_WEAK
import androidx.biometric.BiometricManager.BIOMETRIC_SUCCESS
import androidx.biometric.BiometricPrompt
import androidx.core.content.ContextCompat
import androidx.fragment.app.FragmentActivity
import dagger.hilt.android.qualifiers.ActivityContext
import javax.crypto.Cipher
import javax.inject.Inject

class BiometricAuthHelper @Inject constructor(
    private val biometricCipher: BiometricCipher,
    @ActivityContext private val activityContext: Context
) {

    private val biometricManager = BiometricManager.from(activityContext)

    fun authenticate(
        fragmentActivity: FragmentActivity,
        onSuccess: () -> Unit,
        onError: () -> Unit,
    ) {
        val promptInfo = getPromptInfo()
        val biometricPrompt = getBiometricPrompt(fragmentActivity, onSuccess, onError)

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            when {
                isStrongBiometryAvailable() -> {
                    val cipher = biometricCipher.getCipher()
                    val secretKey = biometricCipher.getSecretKey()
                    cipher.init(Cipher.ENCRYPT_MODE, secretKey)
                    try {
                        biometricPrompt.authenticate(
                            promptInfo,
                            BiometricPrompt.CryptoObject(cipher)
                        )
                    } catch (e: Exception) {
                        Log.e(TAG, "Strong biometry error: ${e.stackTraceToString()}")
                        onError()
                    }
                }

                isWeakBiometryAvailable() -> {
                    try {
                        biometricPrompt.authenticate(promptInfo)
                    } catch (e: Exception) {
                        Log.e(TAG, "Weak biometry error: ${e.stackTraceToString()}")
                        onError()
                    }
                }

                else -> onError()
            }
        }
    }

    private fun isStrongBiometryAvailable(): Boolean {
        return biometricManager.canAuthenticate(BIOMETRIC_STRONG) == BIOMETRIC_SUCCESS
    }

    private fun isWeakBiometryAvailable(): Boolean {
        return biometricManager.canAuthenticate(BIOMETRIC_WEAK) == BIOMETRIC_SUCCESS
    }

    private fun getPromptInfo(): BiometricPrompt.PromptInfo {
        return BiometricPrompt.PromptInfo.Builder()
            .setTitle("Вход по биометрии")
            .setSubtitle("Залогиньтесь с помощью биометрии")
            .setNegativeButtonText("Закрыть")
            .build()
    }

    private fun getBiometricPrompt(
        fragmentActivity: FragmentActivity,
        onSuccess: () -> Unit,
        onError: () -> Unit,
    ): BiometricPrompt {
        val executor = ContextCompat.getMainExecutor(activityContext)
        return BiometricPrompt(
            fragmentActivity,
            executor,
            object : BiometricPrompt.AuthenticationCallback() {
                override fun onAuthenticationError(errorCode: Int, errString: CharSequence) {
                    super.onAuthenticationError(errorCode, errString)
                    onError()
                }

                override fun onAuthenticationSucceeded(result: BiometricPrompt.AuthenticationResult) {
                    super.onAuthenticationSucceeded(result)
                    onSuccess()
                }
            }
        )
    }

    private companion object {
        const val TAG = "BiometricAuthHelper"
    }
}
