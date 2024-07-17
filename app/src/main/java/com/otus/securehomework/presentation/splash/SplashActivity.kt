package com.otus.securehomework.presentation.splash

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.Observer
import androidx.lifecycle.asLiveData
import androidx.lifecycle.lifecycleScope
import com.otus.securehomework.R
import com.otus.securehomework.data.security.biometric.BiometricAuthenticator
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
    lateinit var userPreferences: UserPreferences

    @Inject
    lateinit var biometricAuthenticator: BiometricAuthenticator

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_splash)

        userPreferences.accessToken.asLiveData().observe(this) { token ->
            if (token == null) {
                showBiometric()
            } else {
                navigateToHome()
            }
        }
    }

    private fun showBiometric() {
        lifecycleScope.launch {
            biometricAuthenticator.run(
                activity = this@SplashActivity,
                onSuccess = ::navigateToHome,
                onFailure = ::navigateToAuth,
            )
        }
    }

    private fun navigateToAuth() {
        startNewActivity(AuthActivity::class.java)
    }

    private fun navigateToHome() {
        startNewActivity(HomeActivity::class.java)
    }
}