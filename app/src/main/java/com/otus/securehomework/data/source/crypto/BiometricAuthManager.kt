package com.otus.securehomework.data.source.crypto

import android.content.Context
import android.os.Build
import androidx.annotation.RequiresApi
import androidx.biometric.BiometricManager
import androidx.biometric.auth.AuthPromptHost
import androidx.biometric.auth.Class2BiometricAuthPrompt
import androidx.biometric.auth.Class3BiometricAuthPrompt
import androidx.biometric.auth.authenticate
import com.otus.securehomework.data.dto.LoginData
import com.otus.securehomework.data.source.local.SecureUserPreferences
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.first
import javax.inject.Inject

class BiometricAuthManager @Inject constructor(
    @ApplicationContext private val context: Context,
    private val biometricCipher: BiometricCipher,
    private val userPreferences: SecureUserPreferences
) {

    suspend fun saveBiometricAuth(host: AuthPromptHost) {
        userPreferences.saveBiometricData(getBiometricData(host))
    }

    suspend fun removeBiometricAuth(host: AuthPromptHost) {
        val savedBiometrics = userPreferences.biometricData.first()
        if (savedBiometrics == getBiometricData(host)) userPreferences.removeBiometricData()
    }

    suspend fun checkBiometricAuth(host: AuthPromptHost): LoginData {
        val savedBiometrics = userPreferences.biometricData.first()
        return getBiometricData(host).let {
            if (savedBiometrics == it) it.toLoginData() else LoginData.STUB
        }
    }

    private suspend fun getBiometricData(host: AuthPromptHost) =
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M && canAuthenticate(BiometricManager.Authenticators.BIOMETRIC_STRONG)) {
            strongBiometricAuth(host)
        } else if (canAuthenticate(BiometricManager.Authenticators.BIOMETRIC_WEAK)) {
            weakBiometricAuth(host)
        } else ""

    @RequiresApi(Build.VERSION_CODES.M)
    private suspend fun strongBiometricAuth(host: AuthPromptHost): String {
        val data = userPreferences.tempLoginData.first()
        return Class3BiometricAuthPrompt.Builder("Strong biometry", "dismiss").apply {
            setSubtitle("Input your biometry")
            setDescription("We need your finger")
            setConfirmationRequired(true)
        }
            .build()
            .authenticate(host, biometricCipher.getEncryptor())
            .cryptoObject?.cipher?.let { cipher ->
                String(biometricCipher.encrypt(data.toValue(), cipher).ciphertext)
            } ?: ""
    }

    private suspend fun weakBiometricAuth(host: AuthPromptHost): String {
        val data = userPreferences.tempLoginData.first()
        Class2BiometricAuthPrompt.Builder("Weak biometry", "dismiss").apply {
            setSubtitle("Input your biometry")
            setDescription("We need your finger")
            setConfirmationRequired(true)
        }
            .build()
            .authenticate(host)
        return data.toValue()
    }

    private fun canAuthenticate(authenticator: Int) = BiometricManager.from(context)
        .canAuthenticate(authenticator) == BiometricManager.BIOMETRIC_SUCCESS

    private fun LoginData.toValue() = email + SEPARATOR + password

    private fun String.toLoginData() = split(SEPARATOR).let { LoginData(it[0], it[1]) }

    companion object {
        private const val SEPARATOR = "#*#"
    }

}