package com.otus.securehomework.data.security.biometric

import android.os.Build
import android.widget.Toast
import androidx.annotation.RequiresApi
import androidx.biometric.BiometricManager
import androidx.biometric.BiometricManager.Authenticators.BIOMETRIC_WEAK
import androidx.biometric.BiometricManager.BIOMETRIC_SUCCESS
import androidx.biometric.auth.AuthPromptErrorException
import androidx.biometric.auth.AuthPromptFailureException
import androidx.biometric.auth.AuthPromptHost
import androidx.biometric.auth.Class2BiometricAuthPrompt
import androidx.biometric.auth.Class3BiometricAuthPrompt
import androidx.biometric.auth.authenticate
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentActivity

interface IBiometricController {
    suspend fun authenticateWithBiometric(
        fragment: Fragment,
        doOnError: (throwable: Throwable) -> Unit = {},
        doOnSuccess: () -> Unit
    )

    suspend fun authenticateWithBiometric(
        fragmentActivity: FragmentActivity,
        doOnError: (throwable: Throwable) -> Unit = {},
        doOnSuccess: () -> Unit
    )
}

@RequiresApi(Build.VERSION_CODES.M)
class BiometricControllerImpl(private val biometricCipher: BiometricCipher) : IBiometricController {

    override suspend fun authenticateWithBiometric(
        fragment: Fragment,
        doOnError: (throwable: Throwable) -> Unit,
        doOnSuccess: () -> Unit
    ) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
            authenticateWithStrongBiometric(fragment.requireActivity(), doOnError, doOnSuccess)
        } else {
            authenticateWithWeakBiometric(fragment.requireActivity(), doOnError, doOnSuccess)
        }
    }

    override suspend fun authenticateWithBiometric(
        fragmentActivity: FragmentActivity,
        doOnError: (throwable: Throwable) -> Unit,
        doOnSuccess: () -> Unit
    ) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
            authenticateWithStrongBiometric(fragmentActivity, doOnError, doOnSuccess)
        } else {
            authenticateWithWeakBiometric(fragmentActivity, doOnError, doOnSuccess)
        }
    }

    private suspend fun authenticateWithWeakBiometric(
        fragmentActivity: FragmentActivity,
        doOnError: (throwable: Throwable) -> Unit = {},
        doOnSuccess: () -> Unit
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
            } catch (e: AuthPromptErrorException) {
                doOnError.invoke(e)
            } catch (e: AuthPromptFailureException) {
                doOnError.invoke(e)
            }
        } else {
            Toast.makeText(fragmentActivity, "Biometry not supported", Toast.LENGTH_LONG).show()
        }
    }

    private suspend fun authenticateWithStrongBiometric(
        fragmentActivity: FragmentActivity,
        doOnError: (throwable: Throwable) -> Unit = {},
        doOnSuccess: () -> Unit
    ) {
        val success = BiometricManager.from(fragmentActivity)
            .canAuthenticate(BiometricManager.Authenticators.BIOMETRIC_STRONG) == BIOMETRIC_SUCCESS
        if (success) {
            val encryptor = biometricCipher.getEncryptor()
            val authPrompt = Class3BiometricAuthPrompt
                .Builder("Strong biometry", "dismiss")
                .apply {
                    setSubtitle("Input your biometry")
                    setDescription("We need your finger")
                    setConfirmationRequired(true)
                }
                .build()

            try {
                authPrompt.authenticate(AuthPromptHost(fragmentActivity), encryptor)
                doOnSuccess.invoke()
            } catch (e: AuthPromptErrorException) {
                doOnError.invoke(e)
            } catch (e: AuthPromptFailureException) {
                doOnError.invoke(e)
            }
        } else {
            Toast.makeText(fragmentActivity, "Biometry not supported", Toast.LENGTH_LONG).show()
        }
    }
}

class BiometricControllerStub : IBiometricController {
    override suspend fun authenticateWithBiometric(
        fragment: Fragment,
        doOnError: (throwable: Throwable) -> Unit,
        doOnSuccess: () -> Unit
    ) {
        doOnSuccess.invoke()
    }

    override suspend fun authenticateWithBiometric(
        fragmentActivity: FragmentActivity,
        doOnError: (throwable: Throwable) -> Unit,
        doOnSuccess: () -> Unit
    ) {
        doOnSuccess.invoke()
    }
}