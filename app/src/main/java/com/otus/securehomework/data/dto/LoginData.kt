package com.otus.securehomework.data.dto

data class LoginData(val email: String, val password: String) {
    companion object {
        val STUB = LoginData("", "")
    }
}