package com.otus.securehomework.presentation.auth

import android.content.Context
import android.os.Build
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.Toast
import androidx.annotation.RequiresApi
import androidx.appcompat.app.AlertDialog
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
import androidx.core.widget.addTextChangedListener
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.Observer
import androidx.lifecycle.lifecycleScope
import com.otus.securehomework.R
import com.otus.securehomework.data.Response
import com.otus.securehomework.data.biometrics.BiometricCipher
import com.otus.securehomework.data.crypto.Keys
import com.otus.securehomework.data.crypto.Security
import com.otus.securehomework.presentation.handleApiError
import com.otus.securehomework.presentation.home.HomeActivity
import com.otus.securehomework.presentation.startNewActivity
import com.otus.securehomework.databinding.FragmentLoginBinding
import com.otus.securehomework.presentation.enable
import com.otus.securehomework.presentation.visible
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.Job
import kotlinx.coroutines.launch
import javax.inject.Inject

@AndroidEntryPoint
class LoginFragment : Fragment(R.layout.fragment_login) {

    private lateinit var binding: FragmentLoginBinding
    private val viewModel by viewModels<AuthViewModel>()

     lateinit var keys : Keys
     lateinit var security: Security

    @RequiresApi(Build.VERSION_CODES.M)
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)
        binding = FragmentLoginBinding.bind(view)

        binding.progressbar.visible(false)
        binding.buttonLogin.enable(false)

        keys = Keys(requireActivity().applicationContext)
        security = Security()
        val key = keys.getAesSecretKey()
        val str = "hello world!!!<3"
        val encryptedStr = security.encryptAes(str, key)
        val descryptedStr = security.decryptAes(encryptedStr, key)
        Log.d("TAG", "______ str = $str,   encryptedStr=$encryptedStr, descryptedStr= $descryptedStr")

        viewModel.loginResponse.observe(viewLifecycleOwner, Observer {
            binding.progressbar.visible(it is Response.Loading)
            when (it) {
                is Response.Success -> {
                                lifecycleScope.launch {
                                    showTwoOptionDialog(
                                        context = requireContext(),
                                        title = "Enable biometric authentication?",
                                        message = "Use your biometric to sign in",
                                        positiveButtonText = "enable",
                                        negativeButtonText = "not now",
                                        onNegativeClick = {
                                            requireActivity().startNewActivity(HomeActivity::class.java)
                                        },
                                        onPositiveClick = {
                                            requireActivity().startNewActivity(HomeActivity::class.java)
                                        }
                                    )
                                    viewModel.saveAccessTokens(
                                        it.value.user.access_token!!,
                                        it.value.user.refresh_token!!
                                    )
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
    }

    private fun login() {
        val email = binding.editTextTextEmailAddress.text.toString().trim()
        val password = binding.editTextTextPassword.text.toString().trim()
        viewModel.login(email, password)
    }



    @RequiresApi(Build.VERSION_CODES.M)
    private fun weakBiometric() {
        if (BiometricManager.from(requireContext())
                .canAuthenticate(BIOMETRIC_STRONG) == BIOMETRIC_SUCCESS
        ) {
            val biometricCipher = BiometricCipher(requireActivity().applicationContext)
            val encryptor = biometricCipher.getEncryptor()
            val authPrompt = Class3BiometricAuthPrompt.Builder("Strong biometry", "dismiss").apply {
                setSubtitle("Input your biometry")
                setDescription("We need your finger")
                setConfirmationRequired(true)
            }.build()
            lifecycleScope.launch {
                try {
                    val authResult =
                        authPrompt.authenticate(AuthPromptHost(requireActivity()), encryptor)
                    val encryptedEntity = authResult.cryptoObject?.cipher?.let { cipher ->
                        biometricCipher.encrypt("Secret data", cipher)
                    }
                    Log.d(AuthActivity::class.simpleName, String(encryptedEntity!!.ciphertext))
                    requireActivity().startNewActivity(HomeActivity::class.java)
                } catch (e: AuthPromptErrorException) {
                    Log.e("AuthPromptError", e.message ?: "no message")
                } catch (e: AuthPromptFailureException) {
                    Log.e("AuthPromptFailure", e.message ?: "no message")
                }
            }
        } else if (BiometricManager.from(requireContext())
                .canAuthenticate(BIOMETRIC_WEAK) == BIOMETRIC_SUCCESS
        ) {
            val authPrompt =
                Class2BiometricAuthPrompt.Builder("Weak biometry", "dismiss").apply {
                    setSubtitle("Input your biometry")
                    setDescription("We need your finger")
                    setConfirmationRequired(true)
                }.build()
            lifecycleScope.launch {
                try {
                    authPrompt.authenticate(AuthPromptHost(requireActivity()))
                    Log.d("It works", "Hello from biometry")
                    requireActivity().startNewActivity(HomeActivity::class.java)
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

    fun showTwoOptionDialog(
        context: Context,
        title: String,
        message: String,
        positiveButtonText: String,
        negativeButtonText: String,
        onPositiveClick: () -> Unit,
        onNegativeClick: () -> Unit
    ) {
        val builder = AlertDialog.Builder(context)

        builder.setTitle(title)
        builder.setMessage(message)

        // Устанавливаем положительную кнопку и её обработчик
        builder.setPositiveButton(positiveButtonText) { dialog, _ ->
            onPositiveClick()
            dialog.dismiss() // Закрыть диалог после нажатия кнопки
        }

        // Устанавливаем отрицательную кнопку и её обработчик
        builder.setNegativeButton(negativeButtonText) { dialog, _ ->
            onNegativeClick()
            dialog.dismiss() // Закрыть диалог после нажатия кнопки
        }

        // Создаем и отображаем диалог
        val dialog = builder.create()
        dialog.show()
    }

}


