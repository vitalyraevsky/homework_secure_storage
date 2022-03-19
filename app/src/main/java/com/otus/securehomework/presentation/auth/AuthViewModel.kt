package com.otus.securehomework.presentation.auth

import android.util.Log
import androidx.biometric.auth.AuthPromptHost
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import com.otus.securehomework.data.Response
import com.otus.securehomework.data.dto.LoginResponse
import com.otus.securehomework.data.repository.AuthRepository
import com.otus.securehomework.presentation.BaseViewModel
import com.otus.securehomework.security.biometric.BiometricService
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class AuthViewModel
@Inject constructor(
    private val repository: AuthRepository,
    private val biometricService: BiometricService
) : BaseViewModel(repository) {

    val biometricAvailable = biometricService.isAvailable

    private val _loginResponse: MutableLiveData<Response<LoginResponse>> = MutableLiveData()
    val loginResponse: LiveData<Response<LoginResponse>>
        get() = _loginResponse

    private val _navigateHome: MutableLiveData<Boolean> = MutableLiveData()
    val navigateHome: LiveData<Boolean> get() = _navigateHome

    fun login(
        email: String,
        password: String
    ) = viewModelScope.launch {
        _loginResponse.value = Response.Loading
        _loginResponse.value = repository.login(email, password)
    }

    fun biometric(host: AuthPromptHost) {
        viewModelScope.launch {
            try {
                _navigateHome.postValue(biometricService.authorize(host))
            } catch (ex: Exception) {
                Log.e("AuthViewModel", "Biometric error", ex)
                _navigateHome.postValue(false)
            }
        }
    }

    suspend fun saveAccessTokens(accessToken: String, refreshToken: String) {
        repository.saveAccessTokens(accessToken, refreshToken)
    }
}