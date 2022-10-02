package com.otus.securehomework.presentation.splash

import android.os.Build
import android.util.Log
import androidx.annotation.RequiresApi
import androidx.biometric.BiometricManager
import androidx.biometric.auth.AuthPromptHost
import androidx.biometric.auth.Class2BiometricAuthPrompt
import androidx.biometric.auth.Class3BiometricAuthPrompt
import androidx.biometric.auth.authenticate
import androidx.fragment.app.FragmentActivity
import com.otus.securehomework.data.crypto.BiometricCipher
import javax.inject.Inject

class BiometricAuth @Inject constructor(
    private val biometricManager: BiometricManager,
    private val biometricCipher: BiometricCipher
) {

    fun hasBiometric(): Boolean {
        return hasAuth(
            BiometricManager.Authenticators.BIOMETRIC_STRONG or
                    BiometricManager.Authenticators.BIOMETRIC_WEAK
        )
    }

    suspend fun runBiometric(activity: FragmentActivity, callback: () -> Unit) {
        if (hasAuth(BiometricManager.Authenticators.BIOMETRIC_STRONG) && Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
            checkStrong(activity, callback)
        } else if (hasAuth(BiometricManager.Authenticators.BIOMETRIC_WEAK)) {
            checkWeak(activity, callback)
        }
    }

    @RequiresApi(Build.VERSION_CODES.P)
    private suspend fun checkStrong(activity: FragmentActivity, callback: () -> Unit) {
        val encryptor = biometricCipher.getEncryptor()
        val authPrompt = Class3BiometricAuthPrompt.Builder("Strong biometry", "Dismiss").apply {
            setConfirmationRequired(true)
        }.build()

        try {
            authPrompt.authenticate(AuthPromptHost(activity), encryptor)
            callback()
        } catch (ex: Exception) {
            Log.e(TAG, "Strong biometric auth failed", ex)
        }
    }

    private suspend fun checkWeak(activity: FragmentActivity, callback: () -> Unit) {
        val authPrompt =
            Class2BiometricAuthPrompt.Builder("Weak biometry", "Dismiss").apply {
                setConfirmationRequired(true)
            }.build()

        try {
            authPrompt.authenticate(AuthPromptHost(activity))
            callback()
        } catch (ex: Exception) {
            Log.e(TAG, "Weak biometric auth failed", ex)
        }
    }

    private fun hasAuth(authenticators: Int): Boolean {
        return biometricManager.canAuthenticate(authenticators) == BiometricManager.BIOMETRIC_SUCCESS
    }

    private companion object {
        const val TAG = "BiometricAuth"
    }
}
