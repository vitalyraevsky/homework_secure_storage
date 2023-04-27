package com.otus.securehomework.data.crypto.key_manager

import javax.crypto.SecretKey

interface KeyManager {

    fun getSecretKey(): SecretKey

}