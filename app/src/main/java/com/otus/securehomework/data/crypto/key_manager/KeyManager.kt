package com.otus.securehomework.data.crypto.key_manager

import com.otus.securehomework.data.enums.KeyManagerType
import javax.crypto.SecretKey

interface KeyManager {

    fun getSecretKey(keyName: String, type: KeyManagerType): SecretKey

}