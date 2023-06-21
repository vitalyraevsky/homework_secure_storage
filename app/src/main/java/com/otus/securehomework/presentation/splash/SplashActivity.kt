package com.otus.securehomework.presentation.splash

import android.os.Bundle
import android.util.Log
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.biometric.BiometricManager
import androidx.biometric.BiometricManager.Authenticators.BIOMETRIC_STRONG
import androidx.biometric.BiometricManager.Authenticators.BIOMETRIC_WEAK
import androidx.biometric.auth.AuthPromptErrorException
import androidx.biometric.auth.AuthPromptFailureException
import androidx.biometric.auth.AuthPromptHost
import androidx.biometric.auth.Class2BiometricAuthPrompt
import androidx.biometric.auth.Class3BiometricAuthPrompt
import androidx.biometric.auth.authenticate
import androidx.lifecycle.Observer
import androidx.lifecycle.asLiveData
import androidx.lifecycle.lifecycleScope
import com.otus.securehomework.R
import com.otus.securehomework.data.source.local.UserPreferences
import com.otus.securehomework.presentation.auth.AuthActivity
import com.otus.securehomework.presentation.home.HomeActivity
import com.otus.securehomework.presentation.startNewActivity
import com.otus.securehomework.security.BiometricHelper
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.launch
import javax.inject.Inject

@AndroidEntryPoint
class SplashActivity  : AppCompatActivity() {
    @Inject
    lateinit var userPreferences: UserPreferences
    @Inject
    lateinit var biometricHelper: BiometricHelper

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_splash)

        userPreferences.accessToken.asLiveData().observe(this, Observer {
            if (it == null) {
                startNewActivity(AuthActivity::class.java)
            } else {
                biometricHelper.isBiometricAuthEnabled.asLiveData().observe(this, Observer {
                    if (it) {
                        showBiometricPropmpt()
                    } else {
                        startNewActivity(HomeActivity::class.java)
                    }
                })
            }
        })
    }

    private fun showBiometricPropmpt() {
        val biometricManager = BiometricManager.from(this)
        var canAuth = biometricManager
            .canAuthenticate(BIOMETRIC_STRONG) == BiometricManager.BIOMETRIC_SUCCESS
        if (canAuth) {
            val authPrompt = Class3BiometricAuthPrompt.Builder("Strong biometry", "dismiss").apply {
                setSubtitle("Input your biometry")
                setDescription("We need your finger")
                setConfirmationRequired(true)
            }.build()

            lifecycleScope.launch {
                try {
                    val encryptor = biometricHelper.getEncryptor()

                    authPrompt.authenticate(AuthPromptHost(this@SplashActivity), encryptor)

                    startNewActivity(HomeActivity::class.java)
                } catch (e: AuthPromptErrorException) {
                    Toast.makeText(this@SplashActivity, e.message ?: "no message", Toast.LENGTH_SHORT).show()
                } catch (e: AuthPromptFailureException) {
                    Toast.makeText(this@SplashActivity, e.message ?: "no message", Toast.LENGTH_SHORT).show()
                }
            }
            return
        }
        canAuth = biometricManager
            .canAuthenticate(BIOMETRIC_WEAK) == BiometricManager.BIOMETRIC_SUCCESS
        if (canAuth) {
            val authPrompt = Class2BiometricAuthPrompt.Builder("Weak biometry", "dismiss").apply {
                setSubtitle("Input your biometry")
                setDescription("We need your finger")
                setConfirmationRequired(true)
            }.build()

            lifecycleScope.launch {
                try {
                    authPrompt.authenticate(AuthPromptHost(this@SplashActivity))

                    startNewActivity(HomeActivity::class.java)
                } catch (e: AuthPromptErrorException) {
                    Toast.makeText(this@SplashActivity, e.message ?: "no message", Toast.LENGTH_SHORT).show()
                } catch (e: AuthPromptFailureException) {
                    Toast.makeText(this@SplashActivity, e.message ?: "no message", Toast.LENGTH_SHORT).show()
                }
            }
            return
        }
        Toast.makeText(this, "Biometry not supported", Toast.LENGTH_LONG).show()
    }
}