package com.otus.securehomework.presentation.splash

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.biometric.auth.AuthPromptErrorException
import androidx.lifecycle.Observer
import androidx.lifecycle.asLiveData
import androidx.lifecycle.lifecycleScope
import com.otus.securehomework.R
import com.otus.securehomework.data.myDefence.Biometry
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
    lateinit var biometry: Biometry

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_splash)

        userPreferences.accessToken.asLiveData().observe(this) {
            if (it == null) {
                startNewActivity(AuthActivity::class.java)
            } else {
                lifecycleScope.launch {
                    biometry.authenticateWithBiometric(
                        doOnError = { ex ->
                            when(ex){
                                is AuthPromptErrorException ->{
                                    startNewActivity(AuthActivity::class.java)
                                }
                            }
                        },
                        doOnSuccess = {
                            startNewActivity(HomeActivity::class.java)
                        }
                    )
                }
            }
        }
    }
}