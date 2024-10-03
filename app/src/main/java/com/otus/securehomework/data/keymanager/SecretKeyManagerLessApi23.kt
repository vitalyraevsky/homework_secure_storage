package com.otus.securehomework.data.keymanager

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

class SecretKeyManagerLessApi23 @Inject constructor(
    private val context: Context
) : SecretKeyManager {

    private val sharedPreferences by lazy {
        context.getSharedPreferences(SHARED_PREFERENCE_NAME, Context.MODE_PRIVATE)
    }

    private val keyStore by lazy {
        KeyStore.getInstance(KEY_PROVIDER).apply {
            load(null)
        }
    }

    override fun getSecretKey(): SecretKey {
        return getAesSecretKeyLessThanM() ?: generateAesSecretKey()
    }

    private fun getAesSecretKeyLessThanM(): SecretKey? {
        val encryptedKeyBase64Encoded = getSecretKeyFromSharedPreferences()
        return encryptedKeyBase64Encoded?.let {
            val encryptedKey = Base64.decode(encryptedKeyBase64Encoded, Base64.DEFAULT)
            val key = rsaDecryptKey(encryptedKey)
            SecretKeySpec(key, AES_ALGORITHM)
        }
    }

    private fun getSecretKeyFromSharedPreferences(): String? {
        return sharedPreferences.getString(ENCRYPTED_KEY_NAME, null)
    }

    private fun rsaDecryptKey(encryptedKey: ByteArray?): ByteArray {
        val cipher = Cipher.getInstance(RSA_MODE)
        cipher.init(Cipher.DECRYPT_MODE, getRsaPrivateKey())
        return cipher.doFinal(encryptedKey)
    }

    private fun getRsaPrivateKey(): PrivateKey {
        return keyStore.getKey(RSA_KEY_ALIAS, null) as? PrivateKey ?: generateRsaSecretKey().private
    }

    private fun generateRsaSecretKey(): KeyPair {
        val start = Calendar.getInstance()
        val end = Calendar.getInstance().apply { add(Calendar.YEAR, 30) }

        val spec = KeyPairGeneratorSpec.Builder(context)
            .setAlias(RSA_KEY_ALIAS)
            .setSubject(X500Principal("CN=$RSA_KEY_ALIAS"))
            .setSerialNumber(BigInteger.TEN)
            .setStartDate(start.time)
            .setEndDate(end.time)
            .build()

        return KeyPairGenerator.getInstance(RSA_ALGORITHM, KEY_PROVIDER).run {
            initialize(spec)
            generateKeyPair()
        }
    }

    private fun generateAesSecretKey(): SecretKey {
        val key = ByteArray(16)
        SecureRandom().run {
            nextBytes(key)
        }
        val encryptedKeyBase64encoded = Base64.encodeToString(
            rsaEncryptKey(key),
            Base64.DEFAULT
        )
        sharedPreferences.edit().apply {
            putString(ENCRYPTED_KEY_NAME, encryptedKeyBase64encoded)
            apply()
        }
        return SecretKeySpec(key, AES_ALGORITHM)
    }

    private fun rsaEncryptKey(secret: ByteArray): ByteArray {
        val cipher = Cipher.getInstance(RSA_MODE)
        cipher.init(Cipher.ENCRYPT_MODE, getRsaPublicKey())
        return cipher.doFinal(secret)
    }

    private fun getRsaPublicKey(): PublicKey {
        return keyStore.getCertificate(RSA_KEY_ALIAS)?.publicKey ?: generateRsaSecretKey().public
    }

    private companion object {
        const val KEY_PROVIDER = "AndroidKeyStore"

        const val RSA_ALGORITHM = "RSA"
        const val RSA_KEY_ALIAS = "RSA_KEY_OTUS"
        const val RSA_MODE = "RSA/ECB/PKCS1Padding"
        const val SHARED_PREFERENCE_NAME = "RSAEncryptedKeysSharedPreferences"
        const val ENCRYPTED_KEY_NAME = "RSAEncryptedKeysKeyName"

        const val AES_ALGORITHM = "AES"
    }
}
