package com.otus.securehomework.data.crypto.key

import javax.crypto.SecretKey

interface KeyProvider {
    fun getAesKey(): SecretKey
}