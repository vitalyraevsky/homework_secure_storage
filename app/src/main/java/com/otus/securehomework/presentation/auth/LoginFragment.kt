package com.otus.securehomework.presentation.auth

import android.os.Bundle
import android.util.Log
import android.view.View
import androidx.biometric.BiometricManager
import androidx.biometric.BiometricManager.Authenticators.BIOMETRIC_STRONG
import androidx.biometric.BiometricManager.Authenticators.BIOMETRIC_WEAK
import androidx.biometric.BiometricPrompt
import androidx.core.content.ContextCompat
import androidx.core.widget.addTextChangedListener
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.Observer
import androidx.lifecycle.lifecycleScope
import com.otus.securehomework.R
import com.otus.securehomework.data.Response
import com.otus.securehomework.databinding.FragmentLoginBinding
import com.otus.securehomework.presentation.*
import com.otus.securehomework.presentation.home.HomeActivity
import com.otus.securehomework.security.AesKeystoreWrapperImpl
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.launch
import java.nio.charset.Charset
import javax.inject.Inject


@AndroidEntryPoint
class LoginFragment : Fragment(R.layout.fragment_login) {

    private lateinit var binding: FragmentLoginBinding
    private val viewModel by viewModels<AuthViewModel>()

    @Inject
    lateinit var aesKeystoreWrapper: AesKeystoreWrapperImpl

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
        binding.buttonBiometricLogin.setOnClickListener {
            biometricLogin()
        }
    }

    private fun login() {
        val email = binding.editTextTextEmailAddress.text.toString().trim()
        val password = binding.editTextTextPassword.text.toString().trim()
        viewModel.login(email, password)
    }

    private fun biometricLogin() {
        val biometricManager = BiometricManager.from(requireContext())
        when (biometricManager.canAuthenticate(BIOMETRIC_STRONG or BIOMETRIC_WEAK)) {
            BiometricManager.BIOMETRIC_SUCCESS -> {
                biometricAuthenticate(
                    biometricManager.canAuthenticate(BIOMETRIC_STRONG) == BiometricManager.BIOMETRIC_SUCCESS
                )
            }
            BiometricManager.BIOMETRIC_ERROR_NO_HARDWARE ->
                binding.root.snackbar("No biometric features available on this device.")
            BiometricManager.BIOMETRIC_ERROR_NONE_ENROLLED ->
                binding.root.snackbar("You have not registered any biometric credentials.")
            else ->
                binding.root.snackbar("Biometric features are currently unavailable.")
        }
    }

    private fun createBiometricPrompt(): BiometricPrompt {
        val executor = ContextCompat.getMainExecutor(requireContext())

        val callback = object : BiometricPrompt.AuthenticationCallback() {
            override fun onAuthenticationError(errorCode: Int, errString: CharSequence) {
                super.onAuthenticationError(errorCode, errString)
                Log.d("LoginFragment", "Biometric Authentication error: $errorCode :: $errString")
            }

            override fun onAuthenticationSucceeded(result: BiometricPrompt.AuthenticationResult) {
                super.onAuthenticationSucceeded(result)
                val encryptedInfo = result.cryptoObject?.cipher?.doFinal(
                    "test encrypted data".toByteArray(Charset.defaultCharset())
                )
                Log.d("LoginFragment", "Encrypted information: " + encryptedInfo.contentToString())
                requireActivity().startNewActivity(HomeActivity::class.java)
            }
        }

        return BiometricPrompt(this, executor, callback)
    }

    private fun createPromptInfo(): BiometricPrompt.PromptInfo {
        return BiometricPrompt.PromptInfo.Builder()
            .setTitle("Biometric Authentication")
            .setSubtitle("Log in using your biometric credential")
            .setNegativeButtonText("Use password")
            .build()
    }

    private fun biometricAuthenticate(isBiometricStrong: Boolean) {
        val biometricPrompt = createBiometricPrompt()
        val promptInfo = createPromptInfo()
        if (isBiometricStrong) {
            val cryptoObject = aesKeystoreWrapper.getBiometricCryptoObject()
            biometricPrompt.authenticate(promptInfo, cryptoObject)
        } else biometricPrompt.authenticate(promptInfo)
    }
}