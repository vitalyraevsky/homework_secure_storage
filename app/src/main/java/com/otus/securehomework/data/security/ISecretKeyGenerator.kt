package com.otus.securehomework.data.security

import javax.crypto.SecretKey

interface ISecretKeyGenerator {

    fun generateKey(): SecretKey
}