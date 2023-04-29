package com.otus.securehomework.presentation.splash

import android.annotation.SuppressLint
import android.os.Build
import android.os.Bundle
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.biometric.BiometricManager
import androidx.biometric.BiometricManager.Authenticators.BIOMETRIC_STRONG
import androidx.biometric.BiometricManager.Authenticators.BIOMETRIC_WEAK
import androidx.biometric.BiometricPrompt
import androidx.core.content.ContextCompat
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.asLiveData
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import com.otus.securehomework.R
import com.otus.securehomework.data.biometric.BiometricCipher
import com.otus.securehomework.data.source.local.UserPreferences
import com.otus.securehomework.presentation.auth.AuthActivity
import com.otus.securehomework.presentation.home.HomeActivity
import com.otus.securehomework.presentation.startNewActivity
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.launch
import javax.inject.Inject

@SuppressLint("CustomSplashScreen")
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
            if (it.isNullOrEmpty()) {
                openAuthActivity()
            } else {
                authenticateUser()
            }
        }
    }

    private fun authenticateUser() {
        lifecycleScope.launch {
            lifecycle.repeatOnLifecycle(Lifecycle.State.STARTED) {
                userPreferences.authWithBiometry.collect { isAuthBiometryEnabled ->
                    if (isAuthBiometryEnabled) {
                        authenticateWithBiometric()
                    } else {
                        openHomeActivity()
                    }
                }
            }
        }
    }

    private fun authenticateWithBiometric() {
        val biometricMode = initBiometricMode()
        val biometricManager = BiometricManager.from(this)

        if (biometricManager.canAuthenticate(biometricMode) == BiometricManager.BIOMETRIC_SUCCESS) {
            val biometricPrompt = getBiometricPrompt()
            val promptInfo = getPromptInfo()
            val cipher = biometricCipher.getCipher()
            biometricPrompt.authenticate(promptInfo, BiometricPrompt.CryptoObject(cipher))
        } else {
            Toast.makeText(this, R.string.error_biometry_not_supported, Toast.LENGTH_LONG).show()
        }
    }

    private fun initBiometricMode(): Int {
        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
            BIOMETRIC_STRONG
        } else {
            BIOMETRIC_WEAK
        }
    }

    private fun getPromptInfo(): BiometricPrompt.PromptInfo {
        return BiometricPrompt.PromptInfo.Builder()
            .setTitle(getString(R.string.biometry_authenticate))
            .setSubtitle(getString(R.string.biometric_hint))
            .setNegativeButtonText(getString(R.string.cancel))
            .build()
    }

    private fun getBiometricPrompt(): BiometricPrompt {
        return BiometricPrompt(
            this,
            ContextCompat.getMainExecutor(this),
            object : BiometricPrompt.AuthenticationCallback() {
                override fun onAuthenticationError(errorCode: Int, errString: CharSequence) {
                    super.onAuthenticationError(errorCode, errString)
                    showToast(errString.toString())
                }

                override fun onAuthenticationSucceeded(result: BiometricPrompt.AuthenticationResult) {
                    super.onAuthenticationSucceeded(result)
                    openHomeActivity()
                }

                override fun onAuthenticationFailed() {
                    super.onAuthenticationFailed()
                    showToast(getString(R.string.failed_authentication))
                }
            }
        )
    }

    private fun showToast(message: String) {
        Toast.makeText(this, message, Toast.LENGTH_LONG).show()
    }

    private fun openAuthActivity() = startNewActivity(AuthActivity::class.java)
    private fun openHomeActivity() = startNewActivity(HomeActivity::class.java)

}