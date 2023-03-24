package com.otus.securehomework.security.biometry

import android.content.Context
import android.os.Build
import android.security.keystore.KeyGenParameterSpec
import android.security.keystore.KeyProperties
import android.security.keystore.KeyProperties.KEY_ALGORITHM_AES
import android.security.keystore.KeyProperties.PURPOSE_DECRYPT
import android.security.keystore.KeyProperties.PURPOSE_ENCRYPT
import androidx.annotation.RequiresApi
import androidx.biometric.BiometricManager
import androidx.biometric.auth.AuthPromptErrorException
import androidx.biometric.auth.AuthPromptHost
import androidx.biometric.auth.Class2BiometricAuthPrompt
import androidx.biometric.auth.Class3BiometricAuthPrompt
import androidx.fragment.app.FragmentActivity
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.suspendCancellableCoroutine
import kotlinx.coroutines.withContext
import java.security.KeyStore
import javax.crypto.Cipher
import javax.crypto.KeyGenerator
import javax.crypto.SecretKey
import javax.crypto.spec.IvParameterSpec
import javax.inject.Inject

private const val KEY_NAME = "biometricKey"

@RequiresApi(Build.VERSION_CODES.M)
class BiometricAuthenticator @Inject constructor(
    private val context: Context
) {

    private val ivParameterSpec: IvParameterSpec by lazy {
        IvParameterSpec(
            byteArrayOf(
                101, 102, 103, 104, 105, 106, 107, 108, 109, 110, 111, 112, 113, 114, 115
            )
        )
    }

    private val isStrongBiometricAvailable: Boolean
        get() {
            val biometricManager: BiometricManager = BiometricManager.from(context)
            return biometricManager.canAuthenticate(BiometricManager.Authenticators.BIOMETRIC_STRONG) == BiometricManager.BIOMETRIC_SUCCESS
        }

    private val isWeakBiometricAvailable: Boolean
        get() {
            val biometricManager: BiometricManager = BiometricManager.from(context)
            return biometricManager.canAuthenticate(BiometricManager.Authenticators.BIOMETRIC_WEAK) == BiometricManager.BIOMETRIC_SUCCESS
        }

    @RequiresApi(Build.VERSION_CODES.N)
    suspend fun authenticate(): androidx.biometric.BiometricPrompt.AuthenticationResult {
        return when {
            isStrongBiometricAvailable -> authenticateStrong()
            isWeakBiometricAvailable -> authenticateWeak()
            else -> throw AuthPromptErrorException(
                androidx.biometric.BiometricPrompt.ERROR_NO_BIOMETRICS,
                "Biometric auth not available"
            )
        }
    }

    private suspend fun authenticateWeak(): androidx.biometric.BiometricPrompt.AuthenticationResult {
        val authPrompt = Class2BiometricAuthPrompt.Builder(
            "Biometric auth", "Cancel"
        ).apply {
            setConfirmationRequired(true)
        }.build()
        val host = AuthPromptHost(context as FragmentActivity)
        return suspendCancellableCoroutine {
            authPrompt.startAuthentication(host, CoroutinePromptCallback(it))
        }
    }

    @RequiresApi(Build.VERSION_CODES.N)
    private suspend fun authenticateStrong(): androidx.biometric.BiometricPrompt.AuthenticationResult {
        val authPrompt = Class3BiometricAuthPrompt.Builder(
            "Biometric auth", "Cancel"
        ).apply {
            setConfirmationRequired(true)
        }.build()
        val host = AuthPromptHost(context as FragmentActivity)
        val cipher: Cipher = getCipher()
        cipher.init(Cipher.DECRYPT_MODE, getSecretOrCreateKey(), ivParameterSpec)
        val cryptoObject: androidx.biometric.BiometricPrompt.CryptoObject =
            androidx.biometric.BiometricPrompt.CryptoObject(cipher)
        return suspendCancellableCoroutine {
            authPrompt.startAuthentication(host, cryptoObject, CoroutinePromptCallback(it))
        }
    }

    private fun getCipher(): Cipher = Cipher.getInstance(
        "$KEY_ALGORITHM_AES/${KeyProperties.BLOCK_MODE_CBC}/${KeyProperties.ENCRYPTION_PADDING_PKCS7}"
    )


    @RequiresApi(Build.VERSION_CODES.N)
    private suspend fun generateSecretKey(): SecretKey {
        return withContext(Dispatchers.Default) {
            val spec = KeyGenParameterSpec.Builder(
                KEY_NAME,
                PURPOSE_ENCRYPT or PURPOSE_DECRYPT
            )
                .setBlockModes(KeyProperties.BLOCK_MODE_CBC)
                .setEncryptionPaddings(KeyProperties.ENCRYPTION_PADDING_PKCS7)
                .setUserAuthenticationRequired(true)
                // Invalidate the keys if the user has registered a new biometric
                // credential, such as a new fingerprint. Can call this method only
                // on Android 7.0 (API level 24) or higher. The variable
                // "invalidatedByBiometricEnrollment" is true by default.
                .setInvalidatedByBiometricEnrollment(true)
                .build()
            val keyGenerator = KeyGenerator.getInstance(
                KEY_ALGORITHM_AES, "AndroidKeyStore"
            )
            keyGenerator.init(spec)
            keyGenerator.generateKey()
        }
    }

    @RequiresApi(Build.VERSION_CODES.N)
    private suspend fun getSecretOrCreateKey(): SecretKey {
        val keyStore = KeyStore.getInstance("AndroidKeyStore")
        withContext(Dispatchers.IO) {
            keyStore.load(null)
        }
        return keyStore.getKey(KEY_NAME, null) as? SecretKey? ?: generateSecretKey()
    }
}