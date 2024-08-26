package com.otus.securehomework.presentation.auth

import android.os.Build
import android.os.Bundle
import android.util.Log
import android.widget.Toast
import androidx.annotation.RequiresApi
import androidx.appcompat.app.AppCompatActivity
import androidx.biometric.BiometricManager
import androidx.biometric.BiometricManager.Authenticators.BIOMETRIC_STRONG
import androidx.biometric.BiometricManager.Authenticators.BIOMETRIC_WEAK
import androidx.biometric.BiometricManager.BIOMETRIC_SUCCESS
import androidx.biometric.auth.AuthPromptErrorException
import androidx.biometric.auth.AuthPromptFailureException
import androidx.biometric.auth.AuthPromptHost
import androidx.biometric.auth.Class2BiometricAuthPrompt
import androidx.biometric.auth.Class3BiometricAuthPrompt
import androidx.biometric.auth.authenticate
import androidx.lifecycle.lifecycleScope
import com.otus.securehomework.R
import com.otus.securehomework.data.biometrics.BiometricCipher
import com.otus.securehomework.presentation.home.HomeActivity
import com.otus.securehomework.presentation.startNewActivity
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.launch

@AndroidEntryPoint
class AuthActivity : AppCompatActivity() {

    @RequiresApi(Build.VERSION_CODES.M)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_auth)
//        weakBiometric()
    }

//    @RequiresApi(Build.VERSION_CODES.M)
//    private fun weakBiometric() {
//        if (BiometricManager.from(this)
//                .canAuthenticate(BIOMETRIC_STRONG) == BIOMETRIC_SUCCESS
//        ) {
//            val biometricCipher = BiometricCipher(this.applicationContext)
//            val encryptor = biometricCipher.getEncryptor()
//            val authPrompt = Class3BiometricAuthPrompt.Builder("Strong biometry", "dismiss").apply {
//                setSubtitle("Input your biometry")
//                setDescription("We need your finger")
//                setConfirmationRequired(true)
//            }.build()
//            lifecycleScope.launch {
//                try {
//                    val authResult =
//                        authPrompt.authenticate(AuthPromptHost(this@AuthActivity), encryptor)
//                    val encryptedEntity = authResult.cryptoObject?.cipher?.let { cipher ->
//                        biometricCipher.encrypt("Secret data", cipher)
//                    }
//                    Log.d(AuthActivity::class.simpleName, String(encryptedEntity!!.ciphertext))
//                    this@AuthActivity.startNewActivity(HomeActivity::class.java)
//                } catch (e: AuthPromptErrorException) {
//                    Log.e("AuthPromptError", e.message ?: "no message")
//                } catch (e: AuthPromptFailureException) {
//                    Log.e("AuthPromptFailure", e.message ?: "no message")
//                }
//            }
//        } else if (BiometricManager.from(this@AuthActivity)
//                .canAuthenticate(BIOMETRIC_WEAK) == BIOMETRIC_SUCCESS
//        ) {
//            val authPrompt =
//                Class2BiometricAuthPrompt.Builder("Weak biometry", "dismiss").apply {
//                    setSubtitle("Input your biometry")
//                    setDescription("We need your finger")
//                    setConfirmationRequired(true)
//                }.build()
//            lifecycleScope.launch {
//                try {
//                    authPrompt.authenticate(AuthPromptHost(this@AuthActivity))
//                    Log.d("It works", "Hello from biometry")
//                    this@AuthActivity.startNewActivity(HomeActivity::class.java)
//                } catch (e: AuthPromptErrorException) {
//                    Log.e("AuthPromptError", e.message ?: "no message")
//                } catch (e: AuthPromptFailureException) {
//                    Log.e("AuthPromptFailure", e.message ?: "no message")
//                }
//            }
//        } else {
//            Toast.makeText(this@AuthActivity, "Biometry not supported", Toast.LENGTH_LONG).show()
//        }
//    }
}