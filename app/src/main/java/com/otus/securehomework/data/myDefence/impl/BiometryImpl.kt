package com.otus.securehomework.data.myDefence.impl

import android.os.Build
import android.widget.Toast
import androidx.biometric.BiometricManager
import androidx.biometric.BiometricManager.Authenticators.BIOMETRIC_WEAK
import androidx.biometric.BiometricManager.BIOMETRIC_SUCCESS
import androidx.biometric.BiometricPrompt
import androidx.biometric.auth.AuthPromptHost
import androidx.biometric.auth.Class2BiometricAuthPrompt
import androidx.biometric.auth.Class3BiometricAuthPrompt
import androidx.biometric.auth.authenticate
import androidx.fragment.app.FragmentActivity
import com.otus.securehomework.data.myDefence.Biometry
import com.otus.securehomework.data.myDefence.Security
import javax.inject.Inject

class BiometryImpl @Inject constructor(
    private val activity: FragmentActivity,
    private val security: Security,
) : Biometry {
    override suspend fun authenticateWithBiometric(
        doOnError: (throwable: Throwable) -> Unit,
        doOnSuccess: () -> Unit,
    ) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
            authenticateWithStrongBiometric(activity, doOnError, doOnSuccess)
        } else {
            authenticateWithWeakBiometric(activity, doOnError, doOnSuccess)
        }
    }

    private suspend fun authenticateWithWeakBiometric(
        fragmentActivity: FragmentActivity,
        doOnError: (throwable: Throwable) -> Unit = {},
        doOnSuccess: () -> Unit,
    ) {
        val success = BiometricManager.from(fragmentActivity)
            .canAuthenticate(BIOMETRIC_WEAK) == BIOMETRIC_SUCCESS
        if (success) {
            val authPrompt = Class2BiometricAuthPrompt
                .Builder("Weak biometry", "dismiss")
                .apply {
                    setSubtitle("Input your biometry")
                    setDescription("We need your finger")
                    setConfirmationRequired(true)
                }
                .build()

            try {
                authPrompt.authenticate(AuthPromptHost(fragmentActivity))
                doOnSuccess.invoke()
            } catch (e: Throwable) {
                doOnError.invoke(e)
            }
        } else {
            Toast.makeText(fragmentActivity, "Biometry not supported", Toast.LENGTH_LONG).show()
        }
    }

    private suspend fun authenticateWithStrongBiometric(
        fragmentActivity: FragmentActivity,
        doOnError: (throwable: Throwable) -> Unit = {},
        doOnSuccess: () -> Unit,
    ) {
        val success = BiometricManager.from(fragmentActivity)
            .canAuthenticate(BiometricManager.Authenticators.BIOMETRIC_STRONG) == BIOMETRIC_SUCCESS
        if (success) {
            val encryptor = security.getCipher()
            val authPrompt = Class3BiometricAuthPrompt
                .Builder("Strong biometry", "dismiss")
                .apply {
                    setSubtitle("Input your biometry")
                    setDescription("We need your finger")
                    setConfirmationRequired(true)
                }
                .build()

            try {
                authPrompt.authenticate(
                    AuthPromptHost(fragmentActivity),
                    BiometricPrompt.CryptoObject(encryptor)
                )
                doOnSuccess.invoke()
            } catch (e: Throwable) {
                doOnError.invoke(e)
            }
        } else {
            Toast.makeText(fragmentActivity, "Biometry not supported", Toast.LENGTH_LONG).show()
        }
    }
}