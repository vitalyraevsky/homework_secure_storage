package com.otus.securehomework.crypto.Biometry

data class EncryptedEntity(
    val ciphertext: ByteArray,
    val iv: ByteArray
)