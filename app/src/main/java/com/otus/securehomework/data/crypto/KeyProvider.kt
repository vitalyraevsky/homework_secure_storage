package com.otus.securehomework.data.crypto

import javax.crypto.SecretKey


abstract class KeyProvider {
    abstract fun getAesSecretKey():SecretKey
    protected abstract fun generateAesSecretKey(): SecretKey
}