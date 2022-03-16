package com.otus.securehomework.security

interface KeystoreWrapper {
    fun encryptData(data: ByteArray): Pair<ByteArray, ByteArray>
    fun decryptData(encryptedData: ByteArray, ivBytes: ByteArray): ByteArray
    fun removeKeyStoreKey()
}