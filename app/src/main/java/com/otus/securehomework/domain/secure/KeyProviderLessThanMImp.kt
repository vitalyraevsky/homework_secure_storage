package com.otus.securehomework.domain.secure

import android.content.Context
import android.security.KeyPairGeneratorSpec
import android.util.Base64
import dagger.hilt.android.qualifiers.ApplicationContext
import java.math.BigInteger
import java.security.KeyPair
import java.security.KeyPairGenerator
import java.security.PrivateKey
import java.security.PublicKey
import java.security.SecureRandom
import java.util.Calendar
import javax.crypto.Cipher
import javax.crypto.SecretKey
import javax.crypto.spec.SecretKeySpec
import javax.security.auth.x500.X500Principal

class KeyProviderLessThanMImp(
    @ApplicationContext val context: Context
) : KeyProvider() {

    override val secretKey: SecretKey
        get() = getSecretKeyLessThanM() ?: generateAndSaveAesSecretKeyLessThanM()

    private val sharedPreferences by lazy {
        context.getSharedPreferences(SHARED_PREFERENCE_NAME, Context.MODE_PRIVATE)
    }

    private fun generateAndSaveAesSecretKeyLessThanM(): SecretKey {
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

    private fun getSecretKeyLessThanM(): SecretKey? {
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
        val cipher = Cipher.getInstance(RSA_MODE_LESS_THAN_M)
        cipher.init(Cipher.DECRYPT_MODE, getRsaPrivateKey())
        return cipher.doFinal(encryptedKey)
    }

    private fun rsaEncryptKey(secret: ByteArray): ByteArray {
        val cipher = Cipher.getInstance(RSA_MODE_LESS_THAN_M)
        cipher.init(Cipher.ENCRYPT_MODE, getRsaPublicKey())
        return cipher.doFinal(secret)
    }

    private fun getRsaPrivateKey(): PrivateKey {
        return keyStore.getKey(RSA_KEY_ALIAS, null) as? PrivateKey ?: generateRsaSecretKey().private
    }

    private fun getRsaPublicKey(): PublicKey {
        return keyStore.getCertificate(RSA_KEY_ALIAS)?.publicKey ?: generateRsaSecretKey().public
    }

    private fun generateRsaSecretKey(): KeyPair {
        val start: Calendar = Calendar.getInstance()
        val end: Calendar = Calendar.getInstance()
        end.add(Calendar.YEAR, 30)
        val spec = KeyPairGeneratorSpec.Builder(context)
            .setAlias(RSA_KEY_ALIAS)
            .setSubject(X500Principal("CN=${RSA_KEY_ALIAS}"))
            .setSerialNumber(BigInteger.TEN)
            .setStartDate(start.time)
            .setEndDate(end.time)
            .build()

        return KeyPairGenerator.getInstance(RSA_ALGORITHM, KEY_PROVIDER).run {
            initialize(spec)
            generateKeyPair()
        }
    }

    private companion object {
        private const val KEY_PROVIDER = "AndroidKeyStore"

        private const val RSA_ALGORITHM = "RSA"
        private const val RSA_KEY_ALIAS = "RSA_DEMO"
        private const val RSA_MODE_LESS_THAN_M = "RSA/ECB/PKCS1Padding"
        private const val SHARED_PREFERENCE_NAME = "RSAEncryptedKeysSharedPreferences"
        private const val ENCRYPTED_KEY_NAME = "RSAEncryptedKeysKeyName"

        const val AES_ALGORITHM = "AES"
    }
}
