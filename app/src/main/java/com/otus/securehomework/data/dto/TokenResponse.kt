package com.otus.securehomework.data.dto

data class TokenResponse(
    val access_token: CharSequence?,
    val refresh_token: CharSequence?
)