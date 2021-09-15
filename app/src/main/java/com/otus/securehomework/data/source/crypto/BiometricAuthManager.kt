package com.otus.securehomework.data.source.crypto

import android.content.Context
import android.os.Build
import android.util.Base64
import android.util.Log
import androidx.annotation.RequiresApi
import androidx.biometric.BiometricManager
import androidx.biometric.BiometricPrompt
import androidx.biometric.auth.*
import androidx.fragment.app.FragmentActivity
import com.otus.securehomework.R
import com.otus.securehomework.data.dto.LoginData
import com.otus.securehomework.data.source.local.SecureUserPreferences
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.first
import javax.crypto.Cipher
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
        val decrypted = getBiometricData(host, true)
    }

    suspend fun checkBiometricAuth(host: AuthPromptHost): LoginData {
        val loginData = userPreferences.tempLoginData.first()
        return getBiometricData(host, true).toLoginData().let {
            if (loginData == it) it else LoginData.STUB
        }
    }

    private suspend fun getBiometricData(host: AuthPromptHost, isEncrypted: Boolean = false) =
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M && canAuthenticate(BiometricManager.Authenticators.BIOMETRIC_STRONG)) {
            if (isEncrypted) strongBiometricAuthDecrypt(host) else strongBiometricAuthEncrypt(host)
        } else if (canAuthenticate(BiometricManager.Authenticators.BIOMETRIC_WEAK)) {
            weakBiometricAuth(host)
        } else ""

    @RequiresApi(Build.VERSION_CODES.M)
    private suspend fun strongBiometricAuthEncrypt(host: AuthPromptHost): String {
        val data = userPreferences.tempLoginData.first().toValue()
        return getStrongAuthPrompt()
            .authenticate(host, biometricCipher.getEncryptor())
            .cryptoObject?.cipher?.let { cipher ->
                String(
                    biometricCipher.encrypt(data, cipher).also {
                        userPreferences.saveIv(Base64.encodeToString(it.iv, Base64.DEFAULT))
                    }.ciphertext
                )
            } ?: ""
    }

    @RequiresApi(Build.VERSION_CODES.M)
    private suspend fun strongBiometricAuthDecrypt(host: AuthPromptHost): String {
        val savedBiometrics = userPreferences.biometricData.first()
        val lastIv = Base64.decode(userPreferences.iv.first(), Base64.DEFAULT)
        return getStrongAuthPrompt()
            .authenticate(host, biometricCipher.getDecryptor(lastIv))
            .cryptoObject?.cipher?.let { cipher ->
                biometricCipher.decrypt(savedBiometrics.toByteArray(), cipher)
            } ?: ""
    }

    private fun getStrongAuthPrompt() =
        Class3BiometricAuthPrompt.Builder("Strong biometry", "dismiss")
            .setSubtitle(context.getString(R.string.fingerprint_subtitle))
            .setDescription(context.getString(R.string.fingerprint_description))
            .setConfirmationRequired(true)
            .build()

    private suspend fun weakBiometricAuth(host: AuthPromptHost): String {
        val data = userPreferences.tempLoginData.first()
        Class2BiometricAuthPrompt.Builder("Weak biometry", "dismiss")
            .setSubtitle(context.getString(R.string.fingerprint_subtitle))
            .setDescription(context.getString(R.string.fingerprint_description))
            .setConfirmationRequired(true)
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