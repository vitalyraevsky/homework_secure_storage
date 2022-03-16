package com.otus.securehomework.security

import java.security.*
import javax.crypto.Cipher


class RsaKeystoreWrapperImpl : KeystoreWrapper, BaseKeystoreWrapperImpl() {

    override fun encryptData(data: ByteArray): Pair<ByteArray, ByteArray> {
        val cipher: Cipher = Cipher.getInstance(RSA_PAD_TRANS)
        cipher.init(Cipher.ENCRYPT_MODE, getAsymmetricKeyPair().public)
        return cipher.iv to cipher.doFinal(data)
    }

    override fun decryptData(encryptedData: ByteArray, ivBytes: ByteArray): ByteArray {
        val cipher: Cipher = Cipher.getInstance(RSA_PAD_TRANS)
        cipher.init(Cipher.DECRYPT_MODE, getAsymmetricKeyPair().private)
        return cipher.doFinal(encryptedData)
    }

    private fun getAsymmetricKeyPair(): KeyPair {
        if (!isKeyExists(keyStore)) {
            createAsymmetricKeyPair()
        }

        val privateKey = keyStore.getKey(KEY_ALIAS, null) as PrivateKey
        val publicKey = keyStore.getCertificate(KEY_ALIAS).publicKey

        return KeyPair(publicKey, privateKey)
    }

    private fun createAsymmetricKeyPair(): KeyPair {
        val generator = KeyPairGenerator.getInstance("RSA")
        generator.initialize(2048)
        return generator.generateKeyPair()
    }

    companion object {
        const val RSA_PAD_TRANS = "RSA/ECB/PKCS1Padding"
    }

}
