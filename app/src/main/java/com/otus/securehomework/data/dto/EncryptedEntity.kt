package com.otus.securehomework.data.dto

data class EncryptedEntity(val ciphertext: ByteArray, val iv: ByteArray)