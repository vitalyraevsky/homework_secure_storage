package com.otus.securehomework.data.biometric

import android.content.Context
import android.os.Build
import android.security.keystore.KeyPermanentlyInvalidatedException
import android.widget.Toast
import androidx.biometric.BiometricManager
import androidx.biometric.BiometricManager.Authenticators.BIOMETRIC_STRONG
import androidx.biometric.BiometricManager.Authenticators.BIOMETRIC_WEAK
import androidx.biometric.BiometricPrompt
import androidx.core.content.ContextCompat
import androidx.fragment.app.FragmentActivity
import androidx.lifecycle.DefaultLifecycleObserver
import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.lifecycleScope
import com.otus.securehomework.R
import dagger.hilt.android.qualifiers.ActivityContext
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import javax.crypto.Cipher
import javax.inject.Inject

class BiometricAuthenticator @Inject constructor(
    @ActivityContext private val appContext: Context,
    private val biometricCipher: BiometricCipher
) {
    private val biometricManager by lazy { BiometricManager.from(appContext) }
    private val executor by lazy { ContextCompat.getMainExecutor(appContext) }

    private val isStrongBiometricAvailable: Boolean
        get() = biometricManager.canAuthenticate(BIOMETRIC_STRONG) == BiometricManager.BIOMETRIC_SUCCESS

    private val isWeakBiometricAvailable: Boolean
        get() = biometricManager.canAuthenticate(BIOMETRIC_WEAK) == BiometricManager.BIOMETRIC_SUCCESS

    /**
     * Показ окна с отпечатком пальца
     */
    fun showPrompt(onAuthenticationSucceeded: () -> Unit) {
        val checkCanAuthenticate = checkCanAuthenticate() == BiometricManager.BIOMETRIC_SUCCESS

        if (checkCanAuthenticate) with(appContext as FragmentActivity) {
            lifecycle.addObserver(object : DefaultLifecycleObserver {
                override fun onResume(owner: LifecycleOwner) {
                    lifecycleScope.launch(Dispatchers.Main) {
                        authenticate(this@with, onAuthenticationSucceeded)
                    }
                    lifecycle.removeObserver(this)
                }
            })
        }
    }

    private suspend fun authenticate(
        fragmentActivity: FragmentActivity,
        onAuthenticationSucceeded: () -> Unit
    ) {
        val biometricPrompt = getBiometricPrompt(fragmentActivity, onAuthenticationSucceeded)
        val promptInfo = getPromptInfo()

        if (isStrongBiometricAvailable) {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                val cipher = biometricCipher.getCipher()
                try {
                    val secretKey = biometricCipher.getSecretKey()
                    cipher.init(Cipher.ENCRYPT_MODE, secretKey)
                    biometricPrompt.authenticate(promptInfo, BiometricPrompt.CryptoObject(cipher))
                } catch (e: KeyPermanentlyInvalidatedException) {
                    // Просто обновлю ключ
                    val newSecretKey = biometricCipher.generateAndStoreAesSecretKey()
                    cipher.init(Cipher.ENCRYPT_MODE, newSecretKey)
                    biometricPrompt.authenticate(promptInfo, BiometricPrompt.CryptoObject(cipher))
                } catch (e: Exception) {
                    e.printStackTrace()
                }
            }
        } else if (isWeakBiometricAvailable) {
            biometricPrompt.authenticate(promptInfo)
        }
    }

    /**
     * Проверка на доступность биометрических функций
     */
    private fun checkCanAuthenticate(): Int? {
        return when (
            biometricManager.canAuthenticate(BIOMETRIC_STRONG)
                    or biometricManager.canAuthenticate(BIOMETRIC_WEAK)
        ) {
            BiometricManager.BIOMETRIC_SUCCESS -> BiometricManager.BIOMETRIC_SUCCESS

            BiometricManager.BIOMETRIC_ERROR_SECURITY_UPDATE_REQUIRED,
            BiometricManager.BIOMETRIC_ERROR_UNSUPPORTED,
            BiometricManager.BIOMETRIC_STATUS_UNKNOWN,
            BiometricManager.BIOMETRIC_ERROR_NO_HARDWARE,
            BiometricManager.BIOMETRIC_ERROR_HW_UNAVAILABLE -> {
                showToastShort(appContext, "Проблема с использованием отпечатка пальца")
                null
            }

            BiometricManager.BIOMETRIC_ERROR_NONE_ENROLLED -> {
                showToastShort(appContext, "Нет доступного отпечатка пальца")
                null
            }

            else -> null
        }
    }

    private fun getBiometricPrompt(
        fragmentActivity: FragmentActivity,
        onAuthenticationSucceeded: () -> Unit
    ) = BiometricPrompt(
        fragmentActivity,
        executor,
        object : BiometricPrompt.AuthenticationCallback() {
            override fun onAuthenticationError(errorCode: Int, errString: CharSequence) {
                super.onAuthenticationError(errorCode, errString)
                if (errorCode == BiometricPrompt.ERROR_LOCKOUT) {
                    showToastShort(appContext, errString.toString())
                }
            }

            override fun onAuthenticationSucceeded(result: BiometricPrompt.AuthenticationResult) {
                super.onAuthenticationSucceeded(result)
                onAuthenticationSucceeded()
            }
        }
    )

    private fun getPromptInfo() = BiometricPrompt.PromptInfo.Builder()
        .setTitle("Вход в ${appContext.resources.getString(R.string.app_name)}")
        .setNegativeButtonText("Отмена")
        .build()
}

private fun showToastShort(context: Context?, msg: String?) {
    Toast.makeText(context, msg, Toast.LENGTH_SHORT).show()
}