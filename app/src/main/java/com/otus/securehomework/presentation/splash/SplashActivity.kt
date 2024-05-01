package com.otus.securehomework.presentation.splash

import android.os.Bundle
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.biometric.BiometricPrompt
import androidx.biometric.auth.AuthPromptErrorException
import androidx.biometric.auth.AuthPromptFailureException
import androidx.biometric.auth.AuthPromptHost
import androidx.biometric.auth.authenticate
import androidx.lifecycle.asLiveData
import androidx.lifecycle.lifecycleScope
import com.otus.securehomework.R
import com.otus.securehomework.data.source.local.UserPreferences
import com.otus.securehomework.domain.biometric.BiometricHelper
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
    lateinit var biometricHelper: BiometricHelper

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_splash)

        userPreferences.accessToken.asLiveData().observe(this) {
            if (it == null) {
                startNewActivity(AuthActivity::class.java)
            } else {
                startWithBiometric()
            }
        }
    }

    private fun startWithBiometric() {
        biometricHelper.isAuthEnabled.asLiveData().observe(this) {
            if (it) {
                showBiometricPrompt()
            } else {
                startNewActivity(HomeActivity::class.java)
            }
        }
    }

    private fun showBiometricPrompt() {
        if (biometricHelper.isBiometricStrongEnabled) {
            launchPrompt {
                biometricHelper.strongAuthPrompt.authenticate(
                    host = AuthPromptHost(this@SplashActivity),
                    crypto = biometricHelper.getEncryptor()
                )
            }

        } else if (biometricHelper.isBiometricWeakEnabled) {
            launchPrompt { biometricHelper.wealAuthPrompt.authenticate(AuthPromptHost(this@SplashActivity)) }
        } else {
            Toast.makeText(this, "Biometry not supported", Toast.LENGTH_LONG).show()
        }
    }

    private fun launchPrompt(authPrompt: suspend () -> BiometricPrompt.AuthenticationResult) =
        lifecycleScope.launch {
            try {
                authPrompt.invoke()

                startNewActivity(HomeActivity::class.java)
            } catch (e: AuthPromptErrorException) {
                Toast.makeText(this@SplashActivity, e.message ?: "no message", Toast.LENGTH_SHORT)
                    .show()
            } catch (e: AuthPromptFailureException) {
                Toast.makeText(this@SplashActivity, e.message ?: "no message", Toast.LENGTH_SHORT)
                    .show()
            }
        }
}