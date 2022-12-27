package com.otus.securehomework.presentation.splash

import android.os.Build
import android.os.Bundle
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.biometric.BiometricManager
import androidx.biometric.BiometricManager.Authenticators.BIOMETRIC_STRONG
import androidx.biometric.BiometricManager.Authenticators.BIOMETRIC_WEAK
import androidx.biometric.BiometricManager.BIOMETRIC_SUCCESS
import androidx.biometric.BiometricPrompt
import androidx.biometric.auth.*
import androidx.lifecycle.asLiveData
import androidx.lifecycle.lifecycleScope
import com.otus.securehomework.R
import com.otus.securehomework.data.crypto.BiometricCipher
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

    private val homeActivity = HomeActivity::class.java
    private val authActivity = AuthActivity::class.java

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_splash)

        userPreferences.isAllowBiometry.asLiveData().observe(this) { isAllowBiometry ->
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M && isAllowBiometry) {
                when (BIOMETRIC_SUCCESS) {
                    BiometricManager.from(this).canAuthenticate(BIOMETRIC_STRONG) -> {
                        login {
                            val authPrompt =
                                Class3BiometricAuthPrompt.Builder(STRONG_BIOMETRY_TITLE, NEGATIVE_BUTTON_TEXT).apply {
                                    setSubtitle(SUBTITLE)
                                    setDescription(DESCRIPTION)
                                    setConfirmationRequired(true)
                                }.build()
                            val cryptoObject = BiometricPrompt.CryptoObject(biometricCipher.cipher())
                            authPrompt.authenticate(AuthPromptHost(this@SplashActivity), cryptoObject)
                        }
                    }
                    BiometricManager.from(this).canAuthenticate(BIOMETRIC_WEAK) -> {
                        login {
                            val authPrompt =
                                Class2BiometricAuthPrompt.Builder(WEAK_BIOMETRY_TITLE, NEGATIVE_BUTTON_TEXT).apply {
                                    setSubtitle(SUBTITLE)
                                    setDescription(DESCRIPTION)
                                    setConfirmationRequired(true)
                                }.build()
                            authPrompt.authenticate(AuthPromptHost(this@SplashActivity))
                        }
                    }
                    else -> {
                        Toast.makeText(this, "Biometry not supported", Toast.LENGTH_LONG).show()
                        login {  }
                    }
                }
            } else {
                login {  }
            }
        }
    }

    private fun login(authenticate: suspend () -> Unit) {
        userPreferences.accessToken.asLiveData().observe(this) { token ->
            if (token == null) {
                startNewActivity(authActivity)
            } else {
                lifecycleScope.launch {
                    try {
                        authenticate.invoke()
                        startNewActivity(homeActivity)
                    } catch (e: AuthPromptErrorException) {
                        startNewActivity(authActivity)
                    } catch (e: AuthPromptFailureException) {
                        startNewActivity(authActivity)
                    }
                }
            }
        }
    }

    companion object {
        private const val SUBTITLE = "Input your biometry"
        private const val DESCRIPTION = "We need your finger"
        private const val NEGATIVE_BUTTON_TEXT = "dismiss"
        private const val STRONG_BIOMETRY_TITLE = "Strong biometry"
        private const val WEAK_BIOMETRY_TITLE = "Weak biometry"
    }
}