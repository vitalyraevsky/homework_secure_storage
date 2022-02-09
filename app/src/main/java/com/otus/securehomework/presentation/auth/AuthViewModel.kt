package com.otus.securehomework.presentation.auth

import android.util.Log
import android.widget.Toast
import androidx.biometric.BiometricManager
import androidx.biometric.auth.*
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.viewModelScope
import com.otus.securehomework.data.Response
import com.otus.securehomework.data.dto.LoginResponse
import com.otus.securehomework.data.dto.User
import com.otus.securehomework.data.repository.AuthRepository
import com.otus.securehomework.presentation.BaseViewModel
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class AuthViewModel
@Inject constructor(
    private val repository: AuthRepository
) : BaseViewModel(repository) {

    private val _loginResponse: MutableLiveData<Response<LoginResponse>> = MutableLiveData()
    val loginResponse: LiveData<Response<LoginResponse>>
        get() = _loginResponse

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
}