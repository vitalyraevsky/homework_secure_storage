package com.otus.securehomework.data.dto

data class TokenResponse(
    val access_token: String,
    val refresh_token: String
)