package com.otus.securehomework.presentation.splash

import android.os.Build
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.Observer
import androidx.lifecycle.asLiveData
import com.otus.securehomework.R
import com.otus.securehomework.data.repository.crypto.Keys
import com.otus.securehomework.data.repository.crypto.LegacyKeys
import com.otus.securehomework.data.repository.crypto.SecureUserPreferences
import com.otus.securehomework.data.repository.crypto.Security
import com.otus.securehomework.data.source.local.UserPreferences
import com.otus.securehomework.presentation.auth.AuthActivity
import com.otus.securehomework.presentation.home.HomeActivity
import com.otus.securehomework.presentation.startNewActivity
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class SplashActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_splash)
        val userPreferences = SecureUserPreferences(
            preferences = UserPreferences(this),
            key = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                Keys()
            } else {
                LegacyKeys(applicationContext)
            },
            security = Security()
        )

        userPreferences.accessToken.asLiveData().observe(this, Observer {
            val activity = if (it == null) {
                AuthActivity::class.java
            } else {
                HomeActivity::class.java
            }
            startNewActivity(activity)
        })
    }
}