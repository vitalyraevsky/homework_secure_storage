package com.otus.securehomework.presentation.splash

import android.annotation.SuppressLint
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.biometric.auth.AuthPromptErrorException
import androidx.lifecycle.asLiveData
import androidx.lifecycle.lifecycleScope
import com.otus.securehomework.R
import com.otus.securehomework.data.security.biometric.IBiometricController
import com.otus.securehomework.data.source.local.UserPreferences
import com.otus.securehomework.presentation.auth.AuthActivity
import com.otus.securehomework.presentation.home.HomeActivity
import com.otus.securehomework.presentation.startNewActivity
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.launch
import javax.inject.Inject

@SuppressLint("CustomSplashScreen")
@AndroidEntryPoint
class SplashActivity : AppCompatActivity() {

    @Inject
    lateinit var userPreferences: UserPreferences

    @Inject
    lateinit var biometricController: IBiometricController

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_splash)

        userPreferences.accessToken.asLiveData().observe(this) {
            if (it == null) {
                startNewActivity(AuthActivity::class.java)
            } else {
                lifecycleScope.launch {
                    onUserWillBeAuthenticate()
                }
            }
        }
    }

    private suspend fun onUserWillBeAuthenticate() {
        biometricController.authenticateWithBiometric(
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