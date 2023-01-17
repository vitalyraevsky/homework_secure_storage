package com.otus.securehomework.data.security

import javax.crypto.SecretKey

interface KeyGen {
    fun generate(): SecretKey
}