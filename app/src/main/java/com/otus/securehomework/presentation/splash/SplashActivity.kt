package com.otus.securehomework.presentation.splash

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.biometric.BiometricManager
import androidx.biometric.BiometricPrompt
import androidx.core.content.ContextCompat
import androidx.lifecycle.asLiveData
import com.otus.securehomework.R
import com.otus.securehomework.data.source.local.UserPreferences
import com.otus.securehomework.presentation.auth.AuthActivity
import com.otus.securehomework.presentation.getBiometricAuthenticationOpportunityValue
import com.otus.securehomework.presentation.showMessage
import com.otus.securehomework.presentation.startNewActivity
import dagger.hilt.android.AndroidEntryPoint
import javax.inject.Inject

@AndroidEntryPoint
class SplashActivity  : AppCompatActivity() {

    @Inject
    lateinit var userPreferences: UserPreferences

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_splash)

        userPreferences.accessToken.asLiveData().observe(this) {
            if (it == null) {
                startNewActivity(AuthActivity::class.java)
            } else {
                when (val canAuthenticate = getBiometricAuthenticationOpportunityValue()) {
                    BiometricManager.BIOMETRIC_SUCCESS ->
                        BiometricPrompt(this, ContextCompat.getMainExecutor(this), getAuthCallback())
                            .authenticate(biometricPromptInfo)

                    else -> {
                        startNewActivity(AuthActivity::class.java)
                        handleError(canAuthenticate)
                    }
                }
            }
        }
    }

    private fun handleError(canAuthenticate: Int) {
        val message = getBiometricErrorMessage(canAuthenticate)
        showMessage(message)
    }
}
