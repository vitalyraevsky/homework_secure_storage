package com.otus.securehomework.data.biometric

import com.otus.securehomework.data.crypto.key_manager.KeyManager
import com.otus.securehomework.data.enums.KeyManagerType
import javax.crypto.Cipher
import javax.inject.Inject

class BiometricCipher @Inject constructor(
    private val keyManager: KeyManager
) {

    fun getCipher(): Cipher {
        return Cipher.getInstance(CIPHER_MODE).apply {
            init(
                Cipher.ENCRYPT_MODE,
                keyManager.getSecretKey(
                    keyName = CIPHER_KEY,
                    type = KeyManagerType.BIOMETRIC
                )
            )
        }
    }

    companion object {
        private const val CIPHER_MODE = "AES/CBC/PKCS7Padding"
        private const val CIPHER_KEY = "CIPHER_KEY"
    }

}