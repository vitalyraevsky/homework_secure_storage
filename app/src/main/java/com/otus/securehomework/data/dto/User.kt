package com.otus.securehomework.data.dto

data class User(
    val id: Int,
    val name: String,
    val email: String,
    val email_verified_at: Any,
    val created_at: String,
    val updated_at: String,
    val access_token: CharSequence?,
    val refresh_token: CharSequence?
)