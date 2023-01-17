package com.otus.securehomework.presentation.splash

import android.os.Build
import android.os.Bundle
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.biometric.BiometricManager
import androidx.biometric.BiometricManager.Authenticators.BIOMETRIC_WEAK
import androidx.biometric.BiometricManager.BIOMETRIC_SUCCESS
import androidx.biometric.BiometricPrompt
import androidx.biometric.auth.AuthPromptErrorException
import androidx.biometric.auth.AuthPromptHost
import androidx.biometric.auth.Class2BiometricAuthPrompt
import androidx.biometric.auth.Class3BiometricAuthPrompt
import androidx.biometric.auth.authenticate
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentActivity
import androidx.lifecycle.asLiveData
import androidx.lifecycle.lifecycleScope
import com.otus.securehomework.R
import com.otus.securehomework.data.biometrik.impl.BiometricCipher
import com.otus.securehomework.data.source.local.UserPreferences
import com.otus.securehomework.presentation.auth.AuthActivity
import com.otus.securehomework.presentation.home.HomeActivity
import com.otus.securehomework.presentation.startNewActivity
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.launch
import javax.inject.Inject

@AndroidEntryPoint
class SplashActivity : AppCompatActivity() {

    @Inject
    lateinit var userPreferences: UserPreferences

    @Inject
    lateinit var biometricCipher: BiometricCipher
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_splash)

        userPreferences.accessToken.asLiveData().observe(this) {
            if (it == null) {
                startNewActivity(AuthActivity::class.java)
            } else {
                lifecycleScope.launch {
                    onUserWillBeAuthenticate()
                }
            }
        }
    }

    private suspend fun onUserWillBeAuthenticate() {
        authenticateWithBiometric(
            fragmentActivity = this,
            doOnError = {
                when (it) {
                    is AuthPromptErrorException -> {
                        startNewActivity(AuthActivity::class.java)
                    }
                    else -> {}
                }
            },
            doOnSuccess = {
                startNewActivity(HomeActivity::class.java)
            }
        )
    }

    private suspend fun authenticateWithBiometric(
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
        doOnSuccess: () -> Unit
    ) {
        val success = BiometricManager.from(fragmentActivity)
            .canAuthenticate(BiometricManager.Authenticators.BIOMETRIC_STRONG) == BIOMETRIC_SUCCESS
        if (success) {
            val encryptor = biometricCipher.cipher()
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