package com.otus.securehomework.presentation.splash

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.biometric.auth.AuthPromptErrorException
import androidx.lifecycle.Observer
import androidx.lifecycle.asLiveData
import androidx.lifecycle.lifecycleScope
import com.otus.securehomework.R
import com.otus.securehomework.data.biometric.BiometricController
import com.otus.securehomework.data.source.local.UserPreferences
import com.otus.securehomework.presentation.auth.AuthActivity
import com.otus.securehomework.presentation.home.HomeActivity
import com.otus.securehomework.presentation.startNewActivity
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.launch
import javax.inject.Inject

@AndroidEntryPoint
class SplashActivity : AppCompatActivity() {

    @Inject
    lateinit var userPreferences: UserPreferences

    @Inject
    lateinit var biometricController: BiometricController

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_splash)

        userPreferences.accessToken.asLiveData().observe(this, Observer {
            if (it == null) {
                val activity = AuthActivity::class.java
                startNewActivity(activity)
            } else {
                lifecycleScope.launch {
                    checkUserBiometricPrefs()
                }
            }
        })
    }

    private suspend fun checkUserBiometricPrefs() {
        userPreferences.userBiometricAuth.collect { isBiometricEnable ->
            if (isBiometricEnable) {
                checkUserBiometric()
            } else {
                startNewActivity(HomeActivity::class.java)
            }
        }
    }

    private suspend fun checkUserBiometric() {
        biometricController.authWithBiometric(
            fragmentActivity = this,
            doOnError = {
                when (it) {
                    is AuthPromptErrorException -> {
                        startNewActivity(AuthActivity::class.java)
                    }
                    else -> {}
                }
            },
            doOnSuccess = {
                startNewActivity(HomeActivity::class.java)
            }
        )
    }
}