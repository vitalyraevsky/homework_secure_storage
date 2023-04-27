@file:Suppress("DEPRECATION")

package com.otus.securehomework.data.crypto.key_manager

import android.content.Context
import android.security.KeyPairGeneratorSpec
import android.util.Base64
import java.math.BigInteger
import java.security.KeyPair
import java.security.KeyPairGenerator
import java.security.KeyStore
import java.security.PrivateKey
import java.security.PublicKey
import java.security.SecureRandom
import java.util.Calendar
import javax.crypto.Cipher
import javax.crypto.SecretKey
import javax.crypto.spec.SecretKeySpec
import javax.inject.Inject
import javax.security.auth.x500.X500Principal

class KeyManagerBelowApiMImpl @Inject constructor(
    private val context: Context
) : KeyManager {

    private val keyStore by lazy {
        KeyStore.getInstance(ANDROID_KEY_STORE).apply { load(null) }
    }

    private val sharedPreferences by lazy {
        context.getSharedPreferences(SHARED_PREFERENCE_NAME, Context.MODE_PRIVATE)
    }

    override fun getSecretKey(): SecretKey {
        return getEncryptedSecretKey() ?: generateSecretKey()
    }

    private fun getEncryptedSecretKey(): SecretKey? {
        val encryptedSecretKey = getSecretKeyFromSharedPreferences()
        return encryptedSecretKey?.let {
            val encryptedKey = Base64.decode(encryptedSecretKey, Base64.DEFAULT)
            val key = getDecryptedKey(encryptedKey)
            SecretKeySpec(key, AES_ALGORITHM)
        }
    }

    private fun generateSecretKey(): SecretKey {
        val key = ByteArray(BYTE_SIZE)
        SecureRandom().run {
            nextBytes(key)
        }
        val encodedEncryptedKey = Base64.encodeToString(getEncryptedKey(key), Base64.DEFAULT)
        saveSecretKeyToSharedPreferences(encodedEncryptedKey)

        return SecretKeySpec(key, AES_ALGORITHM)
    }

    private fun getDecryptedKey(encryptedKey: ByteArray?): ByteArray {
        val cipher = Cipher.getInstance(RSA_MODE)
        cipher.init(Cipher.DECRYPT_MODE, getPrivateKey())
        return cipher.doFinal(encryptedKey)
    }

    private fun getEncryptedKey(secret: ByteArray): ByteArray {
        val cipher = Cipher.getInstance(RSA_MODE)
        cipher.init(Cipher.ENCRYPT_MODE, getPublicKey())
        return cipher.doFinal(secret)
    }

    private fun getPrivateKey(): PrivateKey {
        return keyStore.getKey(KEY_ALIAS, null) as? PrivateKey ?: createKeyPair().private
    }

    private fun getPublicKey(): PublicKey {
        return keyStore.getCertificate(KEY_ALIAS)?.publicKey ?: createKeyPair().public
    }

    private fun createKeyPair(): KeyPair {
        val generator = KeyPairGenerator.getInstance(RSA_ALGORITHM, ANDROID_KEY_STORE)

        generator.initialize(getKeyPairGeneratorSpec())
        return generator.generateKeyPair()
    }

    private fun getKeyPairGeneratorSpec(): KeyPairGeneratorSpec {
        val startDate = Calendar.getInstance()
        val endDate = Calendar.getInstance().apply {
            add(Calendar.YEAR, 20)
        }

        return KeyPairGeneratorSpec.Builder(context)
            .setAlias(KEY_ALIAS)
            .setSerialNumber(BigInteger.ONE)
            .setSubject(X500Principal(PRINCIPAL_CERTIFICATE_NAME))
            .setStartDate(startDate.time)
            .setEndDate(endDate.time)
            .build()
    }

    private fun getSecretKeyFromSharedPreferences(): String? {
        return sharedPreferences.getString(ENCRYPTED_KEY_NAME, null)
    }

    private fun saveSecretKeyToSharedPreferences(encryptedKey: String) {
        sharedPreferences.edit().putString(ENCRYPTED_KEY_NAME, encryptedKey).apply()
    }

    companion object {
        private const val SHARED_PREFERENCE_NAME = "RSAEncryptorSharedPreferences"
        private const val ENCRYPTED_KEY_NAME = "RSAEncryptedKeyName"
        private const val ANDROID_KEY_STORE = "AndroidKeyStore"
        private const val AES_ALGORITHM = "AES"
        private const val RSA_ALGORITHM = "RSA"
        private const val RSA_MODE = "RSA/ECB/PKCS1Padding"
        private const val KEY_ALIAS = "RSA_KeyAlias"
        private const val PRINCIPAL_CERTIFICATE_NAME = "CN=$KEY_ALIAS CA Certificate"
        private const val BYTE_SIZE = 16
    }

}