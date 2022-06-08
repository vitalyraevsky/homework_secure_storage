package com.otus.securehomework.presentation.splash

import android.os.Build
import android.os.Bundle
import android.util.Log
import android.widget.Toast
import androidx.annotation.RequiresApi
import androidx.appcompat.app.AppCompatActivity
import androidx.biometric.BiometricManager
import androidx.biometric.BiometricManager.Authenticators.BIOMETRIC_WEAK
import androidx.biometric.BiometricManager.BIOMETRIC_SUCCESS
import androidx.biometric.auth.*
import androidx.lifecycle.lifecycleScope
import com.otus.securehomework.R
import com.otus.securehomework.data.repository.AuthRepository
import com.otus.securehomework.data.source.crypto.BiometricCipher
import com.otus.securehomework.data.source.local.UserPreferences
import com.otus.securehomework.presentation.auth.AuthActivity
import com.otus.securehomework.presentation.home.HomeActivity
import com.otus.securehomework.presentation.startNewActivity
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.launch
import javax.inject.Inject

@AndroidEntryPoint
class SplashActivity  : AppCompatActivity() {

    @Inject
    lateinit var authRepository: AuthRepository

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_splash)
        val userPreferences = UserPreferences(this)

        lifecycleScope.launch{
            val accessToken = authRepository.getDecryptedAccessToken()
            if (accessToken == null) {
                startNewActivity(AuthActivity::class.java)
            } else {
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                    strongBiometry()
                } else {
                    weakBiometry()
                }
            }
        }
    }

    @RequiresApi(Build.VERSION_CODES.M)
    private fun strongBiometry() {
        val success = BiometricManager.from(this)
            .canAuthenticate(BiometricManager.Authenticators.BIOMETRIC_STRONG) == BIOMETRIC_SUCCESS
        if (success) {
            val biometricCipher = BiometricCipher(this.applicationContext)
            val encryptor = biometricCipher.getEncryptor()

            val authPrompt = Class3BiometricAuthPrompt.Builder("Strong biometry", "dismiss").apply {
                setSubtitle("Input your biometry")
                setDescription("We need your finger")
                setConfirmationRequired(true)
            }.build()

            lifecycleScope.launch {
                try {
                    authPrompt.authenticate(AuthPromptHost(this@SplashActivity), encryptor)
                    startNewActivity(HomeActivity::class.java)
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

    private fun weakBiometry() {
        val success = BiometricManager.from(this)
            .canAuthenticate(BIOMETRIC_WEAK) == BIOMETRIC_SUCCESS
        if (success) {
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