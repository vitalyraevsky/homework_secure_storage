package com.otus.securehomework.presentation.auth

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import com.otus.securehomework.data.Response
import com.otus.securehomework.data.dto.LoginResponse
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

    private val _loginResponse: MutableLiveData<State> = MutableLiveData(State(null))
    val loginResponse: LiveData<State>
        get() = _loginResponse

    init {
        isLoggedIn()
    }

    fun login(
        email: String,
        password: String
    ) = viewModelScope.launch {
        _loginResponse.value = _loginResponse.value!!.copy(Response.Loading)
        _loginResponse.value = _loginResponse.value!!.copy(response = repository.login(email, password))
    }

    suspend fun saveAccessTokens(accessToken: String, refreshToken: String) {
        repository.saveAccessTokens(accessToken, refreshToken)
    }

    private fun isLoggedIn(){
        viewModelScope.launch {
            repository.isLoggedIn().collect{
                _loginResponse.value = _loginResponse.value!!.copy(isLoggedIn = it)
            }
        }
    }
}

data class State(
    val response: Response<LoginResponse>?,
    val isLoggedIn: Boolean = false
)