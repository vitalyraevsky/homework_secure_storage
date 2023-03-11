package com.otus.securehomework.presentation.splash

import android.os.Bundle
import android.view.View
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.biometric.auth.AuthPromptErrorException
import androidx.lifecycle.lifecycleScope
import com.otus.securehomework.data.biometric.BiometricHelper
import com.otus.securehomework.data.source.local.UserPreferences
import com.otus.securehomework.databinding.ActivitySplashBinding
import com.otus.securehomework.presentation.auth.AuthActivity
import com.otus.securehomework.presentation.home.HomeActivity
import com.otus.securehomework.presentation.startNewActivity
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import javax.inject.Inject

@AndroidEntryPoint
class SplashActivity : AppCompatActivity() {

    @Inject
    lateinit var userPreferences: UserPreferences

    @Inject
    lateinit var biometricHelper: BiometricHelper

    private lateinit var binding: ActivitySplashBinding

    private var hasToken: Boolean = false
    private var isBioLoginEnabled = false
    private var isBioEnabled = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = ActivitySplashBinding.inflate(layoutInflater)
        setContentView(binding.root)

        binding.btnBiometricAuth.setOnClickListener {
            lifecycleScope.launch {
                launchBiometricAuth()
            }
        }

        init()
    }

    private fun init() {
        lifecycleScope.launch {
            hasToken = userPreferences.accessToken.first()?.isNotEmpty() ?: false
            isBioLoginEnabled = userPreferences.isBiometricLoginEnabled.first()
            isBioEnabled = biometricHelper.isBiometricReady()

            binding.btnBiometricAuth.visibility = if (isBioLoginEnabled && isBioEnabled) {
                View.VISIBLE
            } else {
                View.GONE
            }

            launchBiometricAuth()
        }
    }

    private suspend fun launchBiometricAuth() {
        if (hasToken) {
            if (isBioEnabled && isBioLoginEnabled) {
                try {
                    biometricHelper.showBiometricPrompt("Biometric Login")

                    startNewActivity(HomeActivity::class.java)
                } catch (t: AuthPromptErrorException) {
                    Toast.makeText(this@SplashActivity, t.message, Toast.LENGTH_SHORT).show()
                }
            } else {
                startNewActivity(HomeActivity::class.java)
            }
        } else {
            startNewActivity(AuthActivity::class.java)
        }
    }
}
