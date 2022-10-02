package com.otus.securehomework.presentation.splash

import android.annotation.SuppressLint
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.asLiveData
import androidx.lifecycle.lifecycleScope
import com.otus.securehomework.R
import com.otus.securehomework.data.settings.AppSettings
import com.otus.securehomework.data.source.local.UserPreferences
import com.otus.securehomework.presentation.auth.AuthActivity
import com.otus.securehomework.presentation.home.HomeActivity
import com.otus.securehomework.presentation.startNewActivity
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.launch
import javax.inject.Inject

@AndroidEntryPoint
@SuppressLint("CustomSplashScreen")
class SplashActivity : AppCompatActivity() {

    @Inject
    lateinit var userPreferences: UserPreferences

    @Inject
    lateinit var biometricAuth: BiometricAuth

    @Inject
    lateinit var appSettings: AppSettings

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_splash)

        userPreferences.accessToken.asLiveData().observe(this) {
            val activity = if (it == null) {
                AuthActivity::class.java
            } else {
                HomeActivity::class.java
            }

            if (biometricAuth.hasBiometric() && !it.isNullOrBlank() && appSettings.useBiometry) {
                lifecycleScope.launch {
                    biometricAuth.runBiometric(this@SplashActivity) {
                        startNewActivity(activity)
                    }
                }
            } else {
                startNewActivity(activity)
            }
        }
    }
}