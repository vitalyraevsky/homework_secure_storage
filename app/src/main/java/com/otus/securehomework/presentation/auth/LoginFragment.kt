package com.otus.securehomework.presentation.auth

import androidx.biometric.BiometricManager
import androidx.biometric.BiometricManager.Authenticators.BIOMETRIC_STRONG
import androidx.biometric.BiometricManager.Authenticators.BIOMETRIC_WEAK
import androidx.biometric.BiometricManager.BIOMETRIC_SUCCESS
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.Toast
import androidx.biometric.auth.*
import androidx.core.widget.addTextChangedListener
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.Observer
import androidx.lifecycle.lifecycleScope
import com.otus.myapplication.biometrics.authenticateMy
import com.otus.securehomework.R
import com.otus.securehomework.crypto.Biometry.BiometricCipher
import com.otus.securehomework.data.Response
import com.otus.securehomework.presentation.handleApiError
import com.otus.securehomework.presentation.home.HomeActivity
import com.otus.securehomework.presentation.startNewActivity
import com.otus.securehomework.databinding.FragmentLoginBinding
import com.otus.securehomework.presentation.enable
import com.otus.securehomework.presentation.visible
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.launch

@AndroidEntryPoint
class LoginFragment : Fragment(R.layout.fragment_login) {

    private lateinit var binding: FragmentLoginBinding
    private val viewModel by viewModels<AuthViewModel>()

    override fun onStart() {
        super.onStart()
    }
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)
        binding = FragmentLoginBinding.bind(view)

        binding.progressbar.visible(false)
        binding.buttonLogin.enable(false)

        viewModel.loginResponse.observe(viewLifecycleOwner, Observer {
            binding.progressbar.visible(it is Response.Loading)
            when (it) {
                is Response.Success -> {
                    lifecycleScope.launch {
                        viewModel.saveAccessTokens(
                            it.value.user.access_token!!,
                            it.value.user.refresh_token!!
                        )
                        requireActivity().startNewActivity(HomeActivity::class.java)
                    }
                }
                is Response.Failure -> handleApiError(it) { login() }
            }
        })
        binding.editTextTextPassword.addTextChangedListener {
            val email = binding.editTextTextEmailAddress.text.toString().trim()
            binding.buttonLogin.enable(email.isNotEmpty() && it.toString().isNotEmpty())
        }
        binding.buttonLogin.setOnClickListener {
            login()
        }
        binding.buttonLoginBio.setOnClickListener {
            biometricWeek()
        }
        binding.buttonLoginBioStrong.setOnClickListener {
            biometricStrong()
        }
    }

    private fun biometricWeek() {
        val success = BiometricManager.from(requireActivity())
            .canAuthenticate(BIOMETRIC_WEAK) == BIOMETRIC_SUCCESS
        if (success) {
            val authPrompt = Class2BiometricAuthPrompt.Builder("Вход в приложение ", "использовать пароль").apply {
                setConfirmationRequired(true)
            }.build()

            lifecycleScope.launch {
                try {
                    authPrompt.authenticateMy(AuthPromptHost(requireActivity()))
                    requireActivity().startNewActivity(HomeActivity::class.java)
                    Log.d("It works", "Hello from biometry")
                } catch (e: AuthPromptErrorException) {
                    Log.e("AuthPromptError", e.message ?: "no message")
                } catch (e: AuthPromptFailureException) {
                    Log.e("AuthPromptFailure", e.message ?: "no message")
                }
            }
        } else {
            Toast.makeText(requireActivity(), "Biometry not supported", Toast.LENGTH_LONG).show()
        }
    }

    private fun biometricStrong() {
        val success = BiometricManager.from(requireActivity())
            .canAuthenticate(BIOMETRIC_STRONG) == BIOMETRIC_SUCCESS
        if (success) {
            val biometricCipher = BiometricCipher(requireActivity())
            val encryptor = biometricCipher.getEncryptor()

            val authPrompt = Class3BiometricAuthPrompt.Builder("Strong biometry", "использовать пароль").apply {
                setConfirmationRequired(true)
            }.build()

            lifecycleScope.launch {
                try {
                    val authResult = authPrompt.authenticateMy(AuthPromptHost(requireActivity()), encryptor)
                    val encryptedEntity = authResult.cryptoObject?.cipher?.let { cipher ->
                        biometricCipher.encrypt("Всем привет, это ключ", cipher)
                    }
                    requireActivity().startNewActivity(HomeActivity::class.java)
                    Log.d(LoginFragment::class.simpleName, String(encryptedEntity!!.ciphertext))
                } catch (e: AuthPromptErrorException) {
                    Log.e("AuthPromptError", e.message ?: "no message")
                } catch (e: AuthPromptFailureException) {
                    Log.e("AuthPromptFailure", e.message ?: "no message")
                }
            }
        } else {
            Toast.makeText(requireActivity(), "Biometry not supported", Toast.LENGTH_LONG).show()
        }
    }

    private fun login() {
        val email = binding.editTextTextEmailAddress.text.toString().trim()
        val password = binding.editTextTextPassword.text.toString().trim()
        viewModel.login(email, password)
    }
}