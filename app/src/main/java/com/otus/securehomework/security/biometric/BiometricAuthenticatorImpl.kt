package com.otus.securehomework.security.biometric

import android.content.Context
import android.os.Build
import android.security.keystore.KeyGenParameterSpec
import android.security.keystore.KeyProperties
import androidx.annotation.RequiresApi
import androidx.biometric.BiometricManager
import androidx.biometric.BiometricManager.Authenticators.BIOMETRIC_STRONG
import androidx.biometric.BiometricManager.Authenticators.BIOMETRIC_WEAK
import androidx.biometric.BiometricPrompt
import androidx.biometric.BiometricPrompt.AuthenticationResult
import androidx.biometric.BiometricPrompt.CryptoObject
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
class BiometricAuthenticatorImpl @Inject constructor(
    private val context: Context
): BiometricAuthenticator {

    private val ivSpec: IvParameterSpec by lazy {
        val initVector: ByteArray =
            byteArrayOf(15, 77, 19, 6, 3, 75, -45, 73, 0, 13, -34, 56, 84, 88, 90, 10)
        IvParameterSpec(initVector)
    }

    private val isStrongBiometricAvailable: Boolean
        get() {
            val biometricManager: BiometricManager = BiometricManager.from(context)
            return biometricManager.canAuthenticate(BIOMETRIC_STRONG) == BiometricManager.BIOMETRIC_SUCCESS
        }

    private val isWeakBiometricAvailable: Boolean
        get() {
            val biometricManager: BiometricManager = BiometricManager.from(context)
            return biometricManager.canAuthenticate(BIOMETRIC_WEAK) == BiometricManager.BIOMETRIC_SUCCESS
        }

    @RequiresApi(Build.VERSION_CODES.N)
    override suspend fun authenticate(): AuthenticationResult {
        return when {
            isStrongBiometricAvailable -> authenticate3Class()
            isWeakBiometricAvailable -> authenticate2Class()
            else -> throw AuthPromptErrorException(
                BiometricPrompt.ERROR_NO_BIOMETRICS, "Biometric auth not available"
            )
        }
    }
    private suspend fun authenticate2Class(): AuthenticationResult {
        val authPrompt = Class2BiometricAuthPrompt.Builder(
            "Biometric login for OTUS security app",
            "Use account credential"
        ).apply {
            setSubtitle("Log in using your fingerprint")
            setDescription("We need your finger")
            setConfirmationRequired(true)
        }.build()
        val host: AuthPromptHost = AuthPromptHost(context as FragmentActivity)
        return suspendCancellableCoroutine {
            authPrompt.startAuthentication(host, CoroutinePromptCallback(it))
        }
    }

    @RequiresApi(Build.VERSION_CODES.N)
    private suspend fun authenticate3Class(): AuthenticationResult {
        val authPrompt = Class3BiometricAuthPrompt.Builder(
            "Biometric login for OTUS security app",
            "Use account credential"
        ).apply {
            setSubtitle("Log in using your fingerprint")
            setDescription("We need your finger")
            setConfirmationRequired(true)
        }.build()
        val host: AuthPromptHost = AuthPromptHost(context as FragmentActivity)
        val cipher: Cipher = getCipher()
        cipher.init(Cipher.DECRYPT_MODE, getSecretOrCreateKey(), ivSpec)
        val cryptoObject: CryptoObject = CryptoObject(cipher)
        return suspendCancellableCoroutine {
            authPrompt.startAuthentication(host, cryptoObject, CoroutinePromptCallback(it))
        }
    }

    private fun getCipher(): Cipher {
        return Cipher.getInstance(KeyProperties.KEY_ALGORITHM_AES + "/"
                + KeyProperties.BLOCK_MODE_CBC + "/"
                + KeyProperties.ENCRYPTION_PADDING_PKCS7)
    }

    @RequiresApi(Build.VERSION_CODES.N)
    private suspend fun generateSecretKey(): SecretKey {
        return withContext(Dispatchers.Default) {
            val spec = KeyGenParameterSpec.Builder(
                KEY_NAME,
                KeyProperties.PURPOSE_ENCRYPT or KeyProperties.PURPOSE_DECRYPT)
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
                KeyProperties.KEY_ALGORITHM_AES, "AndroidKeyStore"
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
