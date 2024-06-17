package com.otus.securehomework.presentation.home

import android.os.Bundle
import android.util.Log
import android.widget.Toast
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.biometric.BiometricManager
import androidx.biometric.BiometricPrompt
import androidx.core.content.ContextCompat
import androidx.lifecycle.lifecycleScope
import com.otus.securehomework.R
import com.otus.securehomework.data.source.local.UserPreferences
import com.otus.securehomework.presentation.auth.AuthActivity
import com.otus.securehomework.presentation.startNewActivity
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.launch
import javax.inject.Inject


@AndroidEntryPoint
class HomeActivity : AppCompatActivity() {

    @Inject
    lateinit var userPreferences: UserPreferences

    private val viewModel by viewModels<HomeViewModel>()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_home)

        if (!viewModel.bioWasChecked) authenticateUser()
    }

    fun performLogout() = lifecycleScope.launch {
        viewModel.logout()
        userPreferences.clear()
        startNewActivity(AuthActivity::class.java)
    }

    private fun authenticateUser() {
        val canAuthenticate = BiometricManager.from(this).canAuthenticate(
            BiometricManager.Authenticators.BIOMETRIC_STRONG or
            BiometricManager.Authenticators.BIOMETRIC_WEAK
        )
        when (canAuthenticate) {
            BiometricManager.BIOMETRIC_SUCCESS -> createBiometricPrompt().authenticate(
                createPromptInfo()
            )
            else -> handleBiometricError(canAuthenticate)
        }
    }

    private fun createBiometricPrompt() = BiometricPrompt(
        this,
        ContextCompat.getMainExecutor(this),
        object : BiometricPrompt.AuthenticationCallback() {
            override fun onAuthenticationError(errorCode: Int, errString: CharSequence) {
                super.onAuthenticationError(errorCode, errString)
                showMessage("Authentication error")
                Log.e(TAG, errString.toString())
                performLogout()
            }

            override fun onAuthenticationSucceeded(result: BiometricPrompt.AuthenticationResult) {
                super.onAuthenticationSucceeded(result)
                viewModel.bioWasChecked = true
            }

            override fun onAuthenticationFailed() {
                super.onAuthenticationFailed()
                showMessage("Authentication failed")
                performLogout()
            }
        }
    )

    private fun handleBiometricError(error: Int) {
        val message = when (error) {
            BiometricManager.BIOMETRIC_ERROR_NO_HARDWARE -> "No biometric hardware available"
            BiometricManager.BIOMETRIC_ERROR_HW_UNAVAILABLE -> "Biometric hardware unavailable"
            BiometricManager.BIOMETRIC_ERROR_NONE_ENROLLED -> "No biometric credentials enrolled"
            else -> "Biometric authentication not supported"
        }
        showMessage(message)
        Log.w(TAG, message)
        performLogout()
    }

    private fun createPromptInfo() = BiometricPrompt.PromptInfo.Builder()
        .setTitle("Authentication")
        .setSubtitle("Confirm your identity")
        .setNegativeButtonText("Cancel")
        .setConfirmationRequired(false)
        .build()

    private fun showMessage(message: String) {
        Toast.makeText(this, message, Toast.LENGTH_SHORT).show()
    }

    companion object {
        private const val TAG = "Biometric"
    }

}