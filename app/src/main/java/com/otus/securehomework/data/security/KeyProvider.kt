package com.otus.securehomework.data.security

import javax.crypto.SecretKey

interface KeyProvider {

    fun getAesSecretKey(): SecretKey
}