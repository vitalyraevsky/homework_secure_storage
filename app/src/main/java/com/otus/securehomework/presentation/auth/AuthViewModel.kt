package com.otus.securehomework.presentation.auth

import android.os.Build
import androidx.annotation.RequiresApi
import androidx.biometric.BiometricPrompt
import androidx.fragment.app.FragmentActivity
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.asLiveData
import androidx.lifecycle.viewModelScope
import com.otus.securehomework.data.Response
import com.otus.securehomework.data.dto.LoginResponse
import com.otus.securehomework.data.protection.BiometricManager
import com.otus.securehomework.data.repository.AuthRepository
import com.otus.securehomework.presentation.BaseViewModel
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class AuthViewModel
@Inject constructor(
    private val repository: AuthRepository,
    private val biometricManager: BiometricManager
) : BaseViewModel(repository) {

    private val _loginResponse: MutableLiveData<Response<LoginResponse>> = MutableLiveData()
    val loginResponse: LiveData<Response<LoginResponse>>
        get() = _loginResponse

    val isBiometricEnabled: LiveData<Boolean> = biometricManager.isBiometricEnabled.asLiveData()

    fun login(
        email: String,
        password: String
    ) = viewModelScope.launch {
        _loginResponse.value = Response.Loading
        _loginResponse.value = repository.login(email, password)
    }

    suspend fun saveAccessTokens(accessToken: String, refreshToken: String) {
        repository.saveAccessTokens(accessToken, refreshToken)
    }

    @RequiresApi(Build.VERSION_CODES.M)
    suspend fun saveAccessTokensBiometric(accessToken: String, refreshToken: String) {
        // Шифрование токенов и сохранение их в репозитории с использованием BiometricManager
        val encryptor = biometricManager.getEncryptor().cipher
        encryptor?.let { biometricManager.saveEncryptedData(accessToken, it) }
        repository.saveAccessTokens(accessToken, refreshToken)
    }

    @RequiresApi(Build.VERSION_CODES.M)
    suspend fun decryptAccessToken(): String? {
        // Расшифровка токена с использованием BiometricManager
        val encryptedData = biometricManager.getDecryptor(byteArrayOf()).cipher?.let {
            biometricManager.getEncryptedData(
                it
            )
        }
        return encryptedData
    }

    fun enableBiometricAuth(enable: Boolean) = viewModelScope.launch {
        biometricManager.setBiometricEnabled(enable)
    }

    @RequiresApi(Build.VERSION_CODES.P)
    fun setupBiometricAuth(
        activity: FragmentActivity,
        callback: BiometricPrompt.AuthenticationCallback
    ) {
        biometricManager.setupBiometricPrompt(activity, callback)
    }
}