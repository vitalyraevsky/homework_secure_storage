package com.otus.securehomework.data.security

import javax.crypto.SecretKey

interface IKeyGenerator {

    fun getSecretKey(): SecretKey
}