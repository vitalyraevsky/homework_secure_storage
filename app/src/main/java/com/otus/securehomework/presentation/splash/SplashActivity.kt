package com.otus.securehomework.presentation.splash


import android.os.Build
import android.os.Bundle
import android.util.Log
import android.widget.Toast
import androidx.annotation.RequiresApi
import androidx.appcompat.app.AppCompatActivity
import androidx.biometric.BiometricManager
import androidx.biometric.BiometricManager.Authenticators
import androidx.biometric.auth.AuthPromptErrorException
import androidx.biometric.auth.AuthPromptFailureException
import androidx.biometric.auth.AuthPromptHost
import androidx.biometric.auth.Class2BiometricAuthPrompt
import androidx.biometric.auth.Class3BiometricAuthPrompt
import androidx.biometric.auth.authenticate
import androidx.lifecycle.Observer
import androidx.lifecycle.asLiveData
import androidx.lifecycle.lifecycleScope
import com.otus.securehomework.BuildConfig
import com.otus.securehomework.R
import com.otus.securehomework.data.biometric.BiometricCipher
import com.otus.securehomework.data.crypto.Keys
import com.otus.securehomework.data.crypto.Security
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

        val userPreferences = UserPreferences(
            applicationContext,
            Security(),
            Keys(applicationContext)
        )
        userPreferences.accessToken.asLiveData().observe(this, Observer {
            if (it == null) {
                startNewActivity(AuthActivity::class.java)
            } else {
                lifecycleScope.launch {
                    biometricAuthenticate {
                        startNewActivity(HomeActivity::class.java)
                    }
                }
            }
        })
    }

    @RequiresApi(Build.VERSION_CODES.M)
    private fun strongBiometricAuthenticate(onAuthenticate: () -> Unit) {
        val success = BiometricManager.from(this@SplashActivity)
            .canAuthenticate(Authenticators.BIOMETRIC_STRONG) == BiometricManager.BIOMETRIC_SUCCESS
        if (success) {
            val biometricCipher = BiometricCipher(this@SplashActivity.applicationContext)
            val encryptor = biometricCipher.getEncryptor()

            val authPrompt = Class3BiometricAuthPrompt.Builder("Strong biometry", "dismiss").apply {
                setSubtitle("Input your biometry")
                setDescription("We need your finger")
                setConfirmationRequired(true)
            }.build()

            lifecycleScope.launch {
                try {
                    authPrompt.authenticate(AuthPromptHost(this@SplashActivity), encryptor)
                    onAuthenticate()
                } catch (e: AuthPromptErrorException) {
                    Log.e("AuthPromptError", e.message ?: "no message")
                } catch (e: AuthPromptFailureException) {
                    Log.e("AuthPromptFailure", e.message ?: "no message")
                }
            }
        } else {
            Toast.makeText(this@SplashActivity, "Biometry not supported", Toast.LENGTH_LONG).show()
            if (BuildConfig.DEBUG) {
                onAuthenticate()
            }
        }
    }

    private fun weakBiometricAuthenticate(onAuthenticate: () -> Unit) {
        val success = BiometricManager.from(this@SplashActivity)
            .canAuthenticate(Authenticators.BIOMETRIC_WEAK) == BiometricManager.BIOMETRIC_SUCCESS
        if (success) {
            val authPrompt = Class2BiometricAuthPrompt.Builder("Weak biometry", "dismiss").apply {
                setSubtitle("Input your biometry")
                setDescription("We need your finger")
                setConfirmationRequired(true)
            }.build()

            lifecycleScope.launch {
                try {
                    authPrompt.authenticate(AuthPromptHost(this@SplashActivity))
                    onAuthenticate()
                } catch (e: AuthPromptErrorException) {
                    Log.e("AuthPromptError", e.message ?: "no message")
                } catch (e: AuthPromptFailureException) {
                    Log.e("AuthPromptFailure", e.message ?: "no message")
                }
            }
        } else {
            Toast.makeText(this@SplashActivity, "Biometry not supported", Toast.LENGTH_LONG).show()
            if (BuildConfig.DEBUG) {
                onAuthenticate()
            }
        }
    }


    private fun biometricAuthenticate(onAuthenticate: () -> Unit) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            strongBiometricAuthenticate(onAuthenticate)
        } else {
            weakBiometricAuthenticate(onAuthenticate)
        }
    }
}