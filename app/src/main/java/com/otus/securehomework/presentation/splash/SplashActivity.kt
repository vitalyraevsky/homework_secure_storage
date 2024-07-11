package com.otus.securehomework.presentation.splash

import android.os.Build
import android.os.Bundle
import android.util.Log
import android.widget.Toast
import androidx.annotation.RequiresApi
import androidx.appcompat.app.AppCompatActivity
import androidx.biometric.BiometricManager
import androidx.biometric.BiometricManager.Authenticators.BIOMETRIC_STRONG
import androidx.biometric.BiometricManager.Authenticators.BIOMETRIC_WEAK
import androidx.biometric.BiometricManager.BIOMETRIC_SUCCESS
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
import com.otus.securehomework.biometrics.BiometricCipher
import com.otus.securehomework.data.source.local.UserPreferences
import com.otus.securehomework.presentation.auth.AuthActivity
import com.otus.securehomework.presentation.home.HomeActivity
import com.otus.securehomework.presentation.startNewActivity
import dagger.hilt.android.AndroidEntryPoint
import javax.inject.Inject
import kotlinx.coroutines.launch

@AndroidEntryPoint
class SplashActivity : AppCompatActivity() {

    @Inject
    lateinit var userPreferences: UserPreferences
    @RequiresApi(Build.VERSION_CODES.M)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_splash)

        userPreferences.accessToken.asLiveData().observe(this, Observer {
            if (it == null) {
                startNewActivity(AuthActivity::class.java)
            } else {
                startBiometryLogin()
            }
        })
    }

    @RequiresApi(Build.VERSION_CODES.M)
    fun startBiometryLogin() {
        val successWeak = BiometricManager.from(this)
            .canAuthenticate(BIOMETRIC_WEAK) == BIOMETRIC_SUCCESS
        val successStrong = BiometricManager.from(this)
            .canAuthenticate(BIOMETRIC_STRONG) == BIOMETRIC_SUCCESS
        if (successWeak) {
            val authPrompt = Class2BiometricAuthPrompt.Builder("Weak biometry", "dismiss").apply {
                setSubtitle("Input your biometry")
                setDescription("We need your finger")
                setConfirmationRequired(true)
            }.build()

            lifecycleScope.launch {
                try {
                    authPrompt.authenticate(AuthPromptHost(this@SplashActivity))
                    startNewActivity(HomeActivity::class.java)
                    Log.d("It works", "Hello from weak biometry")
                } catch (e: AuthPromptErrorException) {
                    Log.e("AuthPromptError", e.message ?: "no message")
                } catch (e: AuthPromptFailureException) {
                    Log.e("AuthPromptFailure", e.message ?: "no message")
                }
            }
        } else if (successStrong) {
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
                    Log.d("It works", "Hello from strong biometry")
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


