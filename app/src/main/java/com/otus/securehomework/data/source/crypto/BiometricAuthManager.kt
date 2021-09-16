package com.otus.securehomework.data.source.crypto

import android.content.Context
import android.os.Build
import android.util.Base64
import androidx.annotation.RequiresApi
import androidx.biometric.BiometricManager
import androidx.biometric.auth.AuthPromptHost
import androidx.biometric.auth.Class2BiometricAuthPrompt
import androidx.biometric.auth.Class3BiometricAuthPrompt
import androidx.biometric.auth.authenticate
import com.otus.securehomework.R
import com.otus.securehomework.data.source.local.SecureUserPreferences
import dagger.hilt.android.qualifiers.ApplicationContext
import javax.inject.Inject

class BiometricAuthManager @Inject constructor(
    @ApplicationContext private val context: Context,
    private val biometricCipher: BiometricCipher,
    private val userPreferences: SecureUserPreferences
) {

    suspend fun saveBiometricAuth(host: AuthPromptHost) {
        userPreferences.saveHasBiometrics(getBiometricData(host))
    }

    suspend fun checkBiometricAuth(host: AuthPromptHost): Boolean {
        return getBiometricData(host)
    }

    private suspend fun getBiometricData(host: AuthPromptHost) =
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M && canAuthenticate(BiometricManager.Authenticators.BIOMETRIC_STRONG)) {
            strongBiometricAuth(host)
        } else if (canAuthenticate(BiometricManager.Authenticators.BIOMETRIC_WEAK)) {
            weakBiometricAuth(host)
        } else false

    @RequiresApi(Build.VERSION_CODES.M)
    private suspend fun strongBiometricAuth(host: AuthPromptHost): Boolean {
        Class3BiometricAuthPrompt.Builder("Strong biometry", "dismiss")
            .setSubtitle(context.getString(R.string.fingerprint_subtitle))
            .setDescription(context.getString(R.string.fingerprint_description))
            .setConfirmationRequired(true)
            .build()
            .auth(host, biometricCipher.getEncryptor())
        return true
    }

    private suspend fun weakBiometricAuth(host: AuthPromptHost): Boolean {
        Class2BiometricAuthPrompt.Builder("Weak biometry", "dismiss")
            .setSubtitle(context.getString(R.string.fingerprint_subtitle))
            .setDescription(context.getString(R.string.fingerprint_description))
            .setConfirmationRequired(true)
            .build()
            .auth(host)
        return true
    }

    private fun canAuthenticate(authenticator: Int) = BiometricManager.from(context)
        .canAuthenticate(authenticator) == BiometricManager.BIOMETRIC_SUCCESS

    fun ByteArray.toBase64(flags: Int = Base64.DEFAULT): String = Base64.encodeToString(this, flags)
    fun String.fromBase64(flags: Int = Base64.DEFAULT): ByteArray = Base64.decode(this, flags)
}