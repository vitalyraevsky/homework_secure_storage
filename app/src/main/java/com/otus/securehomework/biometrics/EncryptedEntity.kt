package com.otus.securehomework.biometrics

//https://github.com/vitalyraevsky/otus_security/blob/master/app/src/main/java/com/otus/myapplication/biometrics/EncryptedEntity.kt
data class EncryptedEntity(
    val ciphertext: ByteArray,
    val iv: ByteArray
)