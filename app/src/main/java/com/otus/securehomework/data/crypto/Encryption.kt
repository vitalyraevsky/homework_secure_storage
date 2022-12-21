package com.otus.securehomework.data.crypto

interface Encryption {

    suspend fun encrypt(bytes: ByteArray?): EncryptionResult

    suspend fun decrypt(encryptedBytes: ByteArray?, iv: ByteArray): ByteArray
}