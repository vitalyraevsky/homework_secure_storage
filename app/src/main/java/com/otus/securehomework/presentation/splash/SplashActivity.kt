package com.otus.securehomework.presentation.splash

import android.os.Build
import android.os.Bundle
import android.util.Log
import android.widget.Toast
import androidx.annotation.RequiresApi
import androidx.appcompat.app.AppCompatActivity
import androidx.biometric.BiometricManager
import androidx.biometric.auth.*
import androidx.lifecycle.asLiveData
import androidx.lifecycle.lifecycleScope
import com.otus.securehomework.R
import com.otus.securehomework.data.repository.biometric.BiometricCipher
import com.otus.securehomework.data.repository.crypto.Keys
import com.otus.securehomework.data.repository.crypto.LegacyKeys
import com.otus.securehomework.data.repository.crypto.SecureUserPreferences
import com.otus.securehomework.data.repository.crypto.Security
import com.otus.securehomework.data.source.local.UserPreferences
import com.otus.securehomework.presentation.auth.AuthActivity
import com.otus.securehomework.presentation.home.HomeActivity
import com.otus.securehomework.presentation.startNewActivity
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.launch

@AndroidEntryPoint
class SplashActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_splash)
        val userPreferences = SecureUserPreferences(
            preferences = UserPreferences(this),
            key = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                Keys()
            } else {
                LegacyKeys(applicationContext)
            },
            security = Security()
        )

        userPreferences.accessToken.asLiveData().observe(this) {
            when (it) {
                null -> startNewActivity(AuthActivity::class.java)
                else -> checkBiometric {
                    startNewActivity(HomeActivity::class.java)
                }
            }
        }
    }

    private fun checkBiometric(onSuccess: () -> Unit) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            checkStrong(onSuccess)
        } else {
            checkWeek(onSuccess)
        }
    }

    @RequiresApi(Build.VERSION_CODES.M)
    private fun checkStrong(onSuccess: () -> Unit) {
        val success = BiometricManager.from(this)
            .canAuthenticate(BiometricManager.Authenticators.BIOMETRIC_STRONG) == BiometricManager.BIOMETRIC_SUCCESS
        if (success) {
            val biometricCipher = BiometricCipher(this.applicationContext)
            val encryptor = biometricCipher.getEncryptor()

            val authPrompt =
                Class3BiometricAuthPrompt.Builder("Strong biometry", "dismiss").apply {
                    setSubtitle("Input your biometry")
                    setDescription("We need your finger")
                    setConfirmationRequired(true)
                }.build()

            lifecycleScope.launch {
                try {
                    authPrompt.authenticate(AuthPromptHost(this@SplashActivity), encryptor)
                    onSuccess()
                } catch (e: AuthPromptErrorException) {
                    Log.e("AuthPromptError", e.message ?: "no message")
                } catch (e: AuthPromptFailureException) {
                    Log.e("AuthPromptFailure", e.message ?: "no message")
                }
            }
        }
    }

    private fun checkWeek(onSuccess: () -> Unit) {
        val success = BiometricManager.from(this)
            .canAuthenticate(BiometricManager.Authenticators.BIOMETRIC_WEAK) == BiometricManager.BIOMETRIC_SUCCESS
        if (success) {
            val authPrompt =
                Class2BiometricAuthPrompt.Builder("Weak biometry", "dismiss").apply {
                    setSubtitle("Input your biometry")
                    setDescription("We need your finger")
                    setConfirmationRequired(true)
                }.build()

            lifecycleScope.launch {
                try {
                    authPrompt.authenticate(AuthPromptHost(this@SplashActivity))
                    onSuccess()
                } catch (e: AuthPromptErrorException) {
                    Log.e("AuthPromptError", e.message ?: "no message")
                } catch (e: AuthPromptFailureException) {
                    Log.e("AuthPromptFailure", e.message ?: "no message")
                }
            }
        } else {
            Toast.makeText(this, "Biometry not supported", Toast.LENGTH_LONG).show()
        }
    }

}