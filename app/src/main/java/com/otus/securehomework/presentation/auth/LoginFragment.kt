package com.otus.securehomework.presentation.auth

import android.os.Build
import android.os.Bundle
import android.view.View
import android.widget.Toast
import androidx.annotation.RequiresApi
import androidx.biometric.BiometricPrompt
import androidx.core.widget.addTextChangedListener
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.Observer
import androidx.lifecycle.lifecycleScope
import com.otus.securehomework.R
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
                Response.Loading -> Unit
            }
        })
        binding.editTextTextPassword.addTextChangedListener {
            val email = binding.editTextTextEmailAddress.text.toString().trim()
            binding.buttonLogin.enable(email.isNotEmpty() && it.toString().isNotEmpty())
        }
        binding.buttonLogin.setOnClickListener {
            login()
        }

        // Проверяем, включена ли биометрическая аутентификация, и запускаем её
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
            viewModel.isBiometricEnabled.observe(viewLifecycleOwner, Observer { enabled ->
                if (enabled) {
                    viewModel.setupBiometricAuth(requireActivity(), biometricCallback)
                }
            })

            viewModel.enableBiometricAuth(true)
        }
    }

    private fun login() {
        val email = binding.editTextTextEmailAddress.text.toString().trim()
        val password = binding.editTextTextPassword.text.toString().trim()
        viewModel.login(email, password)
    }

    private fun showToast(message: String){
        Toast.makeText(context, message, Toast.LENGTH_LONG).show()
    }

    // Callback для обработки результатов биометрической аутентификации
    private val biometricCallback = object : BiometricPrompt.AuthenticationCallback() {
        @RequiresApi(Build.VERSION_CODES.M)
        override fun onAuthenticationSucceeded(result: BiometricPrompt.AuthenticationResult) {
            super.onAuthenticationSucceeded(result)
            // Здесь вы можете получить доступ к расшифрованным данным и продолжить процесс входа
            lifecycleScope.launch {
                val decryptedToken = viewModel.decryptAccessToken()
                if (decryptedToken != null) {
                    // Продолжить с использованием расшифрованного токена
                    requireActivity().startNewActivity(HomeActivity::class.java)
                }
            }
        }

        override fun onAuthenticationFailed() {
            showToast("Biometry not supported");
            super.onAuthenticationFailed()
            // Обработка ошибки аутентификации
        }
    }
}