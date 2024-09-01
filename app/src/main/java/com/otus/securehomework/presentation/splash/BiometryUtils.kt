package com.otus.securehomework.presentation.splash

import android.app.Activity
import androidx.biometric.BiometricManager
import androidx.biometric.BiometricPrompt
import com.otus.securehomework.presentation.auth.AuthActivity
import com.otus.securehomework.presentation.home.HomeActivity
import com.otus.securehomework.presentation.showMessage
import com.otus.securehomework.presentation.startNewActivity

internal val biometricPromptInfo = BiometricPrompt.PromptInfo.Builder()
    .setTitle("Authentication")
    .setSubtitle("Confirm your identity")
    .setNegativeButtonText("Cancel")
    .setConfirmationRequired(false)
    .build()

internal fun Activity.getAuthCallback() = object : BiometricPrompt.AuthenticationCallback() {
    override fun onAuthenticationSucceeded(result: BiometricPrompt.AuthenticationResult) {
        showMessage("Authentication successful")
        startNewActivity(HomeActivity::class.java)
    }

    override fun onAuthenticationError(errorCode: Int, errString: CharSequence) {
        showMessage("Unrecoverable error => $errString")
        startNewActivity(AuthActivity::class.java)
    }

    override fun onAuthenticationFailed() {
        showMessage("Could not recognise the user")
        startNewActivity(AuthActivity::class.java)
    }
}

internal fun getBiometricErrorMessage(canAuthenticate: Int) = when (canAuthenticate) {
    BiometricManager.BIOMETRIC_ERROR_NO_HARDWARE ->
        "There is no suitable hardware"

    BiometricManager.BIOMETRIC_ERROR_HW_UNAVAILABLE ->
        "The hardware is unavailable. Try again later"

    BiometricManager.BIOMETRIC_ERROR_NONE_ENROLLED ->
        "No biometric or device credential is enrolled"

    BiometricManager.BIOMETRIC_ERROR_SECURITY_UPDATE_REQUIRED ->
        "A security vulnerability has been discovered with one or more hardware sensors"

    BiometricManager.BIOMETRIC_ERROR_UNSUPPORTED ->
        "The specified options are incompatible with the current Android version"

    BiometricManager.BIOMETRIC_STATUS_UNKNOWN ->
        "Unable to determine whether the user can authenticate"

    else -> "Biometric Error"
}
