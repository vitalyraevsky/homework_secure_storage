package com.otus.securehomework.presentation.splash

import android.os.Bundle
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.biometric.BiometricManager
import androidx.biometric.BiometricPrompt
import androidx.lifecycle.Observer
import androidx.lifecycle.asLiveData
import com.otus.securehomework.R
import com.otus.securehomework.data.crypto.SecuredTokenStorage
import com.otus.securehomework.data.repository.BiometricRepository
import com.otus.securehomework.data.repository.BiometricState
import com.otus.securehomework.data.source.local.UserPreferences
import com.otus.securehomework.presentation.auth.AuthActivity
import com.otus.securehomework.presentation.home.HomeActivity
import com.otus.securehomework.presentation.startNewActivity
import dagger.hilt.android.AndroidEntryPoint
import javax.inject.Inject

@AndroidEntryPoint
class SplashActivity  : AppCompatActivity() {

    @Inject
    lateinit var tokenStorage: SecuredTokenStorage

    @Inject
    lateinit var biometricRepository: BiometricRepository

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_splash)
        val userPreferences = UserPreferences(this)

        tokenStorage.getAccessToken().asLiveData().observe(this, Observer {
            if (it == null) {
                startNewActivity(AuthActivity::class.java)
            } else {
                checkBiometric()
            }
        })



    }

    private fun checkBiometric(){
        val biometricState = biometricRepository.getBiometricState()

        if (biometricState == BiometricState.OFF){
            startNewActivity(HomeActivity::class.java)
            return
        }

        val biometricMode = when(biometricState){
            BiometricState.STRONG -> BiometricManager.Authenticators.BIOMETRIC_STRONG
            else -> BiometricManager.Authenticators.BIOMETRIC_WEAK
        }

        val biometricManager = BiometricManager.from(this)

        if(biometricManager.canAuthenticate(biometricMode) == BiometricManager.BIOMETRIC_SUCCESS){

            val biometricPrompt = BiometricPrompt(this, object: BiometricPrompt.AuthenticationCallback() {
                override fun onAuthenticationError(errorCode: Int, errString: CharSequence) {
                    super.onAuthenticationError(errorCode, errString)
                    Toast.makeText(this@SplashActivity, errString, Toast.LENGTH_LONG).show()
                }

                override fun onAuthenticationSucceeded(result: BiometricPrompt.AuthenticationResult) {
                    super.onAuthenticationSucceeded(result)
                    startNewActivity(HomeActivity::class.java)
                }

                override fun onAuthenticationFailed() {
                    super.onAuthenticationFailed()
                    Toast.makeText(this@SplashActivity, "onAuthenticationFailed", Toast.LENGTH_LONG).show()
                }
            })

            val promptInfo = BiometricPrompt.PromptInfo.Builder()
                .setTitle("Biometric login for ShutApp")
                .setSubtitle("Log in using your biometric credential")
                .setNegativeButtonText("Use PIN instead")
                .build()

            biometricPrompt.authenticate(promptInfo)
        }



    }

}