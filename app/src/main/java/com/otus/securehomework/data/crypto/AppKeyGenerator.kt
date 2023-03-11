package com.otus.securehomework.data.crypto

import javax.crypto.SecretKey

interface AppKeyGenerator {

    fun getSecretKey(): SecretKey

}