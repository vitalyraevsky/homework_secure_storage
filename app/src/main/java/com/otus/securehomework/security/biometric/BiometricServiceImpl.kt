package com.otus.securehomework.security.biometric

import android.content.Context
import android.os.Build
import androidx.biometric.BiometricManager
import androidx.biometric.auth.AuthPromptHost
import androidx.biometric.auth.Class2BiometricAuthPrompt
import androidx.biometric.auth.Class3BiometricAuthPrompt
import com.otus.securehomework.security.EncryptionService
import dagger.hilt.android.qualifiers.ApplicationContext
import javax.inject.Inject

class BiometricServiceImpl @Inject constructor(
    @ApplicationContext private val context: Context,
    private val encryptionService: EncryptionService
) : BiometricService {

    private val manager by lazy {
        BiometricManager.from(context)
    }

    override val isAvailable: Boolean =
        canAuthenticate(BiometricManager.Authenticators.BIOMETRIC_STRONG) || canAuthenticate(
            BiometricManager.Authenticators.BIOMETRIC_WEAK
        )

    override suspend fun authorize(host: AuthPromptHost): Boolean {
        return when {
            Build.VERSION.SDK_INT >= Build.VERSION_CODES.M && canAuthenticate(BiometricManager.Authenticators.BIOMETRIC_STRONG) -> {
                strongAuth(host)
                true
            }
            canAuthenticate(BiometricManager.Authenticators.BIOMETRIC_WEAK) -> {
                weakAuth(host)
                true
            }
            else -> false
        }
    }

    private suspend fun strongAuth(host: AuthPromptHost) =
        Class3BiometricAuthPrompt.Builder("Strong biometry", "Close")
            .setConfirmationRequired(true)
            .build()
            .authenticate(host, encryptionService.getBiometricCryptoObject())

    private suspend fun weakAuth(host: AuthPromptHost) =
        Class2BiometricAuthPrompt.Builder("Weak biometry", "Close")
            .setConfirmationRequired(true)
            .build()
            .authenticate(host)

    private fun canAuthenticate(method: Int): Boolean {
        return when (manager.canAuthenticate(method)) {
            BiometricManager.BIOMETRIC_SUCCESS -> true
            else -> false
        }
    }
}