package com.otus.securehomework.data.repository.crypto

import javax.crypto.SecretKey

interface IKeyProvider {
    fun getAesSecretKey(): SecretKey
}