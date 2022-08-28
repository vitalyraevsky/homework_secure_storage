package com.otus.securehomework.data.repository.crypto


import android.content.Context
import android.security.KeyPairGeneratorSpec
import android.util.Base64
import java.math.BigInteger
import java.security.*
import java.util.*
import javax.crypto.Cipher
import javax.crypto.SecretKey
import javax.crypto.spec.SecretKeySpec
import javax.security.auth.x500.X500Principal

class LegacyKeys(
    private val applicationContext: Context
) : IKeyProvider {

    private val mKeyStore by lazy {
        KeyStore.getInstance(Keys.ANDROID_KEY_STORE).apply {
            load(null)
        }
    }
    private val sharedPreferences by lazy {
        applicationContext.getSharedPreferences(SHARED_PREFERENCE_NAME, Context.MODE_PRIVATE)
    }

    override fun getAesSecretKey(): SecretKey {
        val encryptedKeyBase64Encoded = getSecretKeyFromSharedPreferences()

        return encryptedKeyBase64Encoded?.let {
            val encryptedKey = Base64.decode(encryptedKeyBase64Encoded, Base64.DEFAULT)
            val key = rsaDecryptKey(encryptedKey)
            SecretKeySpec(key, AES_ALGORITHM)
        } ?: generateAesKey()
    }

    private fun getSecretKeyFromSharedPreferences() =
        sharedPreferences.getString(ENCRYPTED_KEY_NAME, null)

    private fun rsaDecryptKey(encryptedKey: ByteArray): ByteArray? {
        val cipher = Cipher.getInstance(RSA_MODE_LESS_THAN_M)
        val rsaPrivateKey = getRsaPrivateKey()
        cipher.init(Cipher.DECRYPT_MODE, rsaPrivateKey)
        return cipher.doFinal(encryptedKey)
    }

    private fun getRsaPrivateKey(): PrivateKey {
        return mKeyStore.getKey(RSA_KEY_ALIAS, null) as? PrivateKey
            ?: generateRsaSecretKey().private
    }

    private fun generateAesKey(): SecretKey {
        val key = ByteArray(16)
        SecureRandom().run {
            nextBytes(key)
        }
        val encryptedKey = rsaEncryptKey(key)
        val encryptedKeyBase64encoded = Base64.encodeToString(encryptedKey, Base64.DEFAULT)
        sharedPreferences.edit().apply {
            putString(ENCRYPTED_KEY_NAME, encryptedKeyBase64encoded)
            apply()
        }
        return SecretKeySpec(key, AES_ALGORITHM)
    }

    private fun rsaEncryptKey(secret: ByteArray): ByteArray {
        val cipher = Cipher.getInstance(RSA_MODE_LESS_THAN_M)
        val rsaPublicKey = getRsaPublicKey()
        cipher.init(Cipher.ENCRYPT_MODE, rsaPublicKey)
        return cipher.doFinal(secret)
    }

    private fun getRsaPublicKey(): PublicKey {
        return mKeyStore.getCertificate(RSA_KEY_ALIAS)?.publicKey
            ?: generateRsaSecretKey().public
    }

    private fun generateRsaSecretKey(): KeyPair {
        val start = Calendar.getInstance()
        val end = Calendar.getInstance()
        end.add(Calendar.YEAR, 30)
        val spec = KeyPairGeneratorSpec.Builder(applicationContext)
            .setAlias(RSA_KEY_ALIAS)
            .setSubject(X500Principal("CN=$RSA_KEY_ALIAS"))
            .setSerialNumber(BigInteger.TEN)
            .setStartDate(start.time)
            .setEndDate(end.time)
            .build()

        return KeyPairGenerator.getInstance(RSA_ALGORITHM).run {
            initialize(spec)
            generateKeyPair()
        }
    }

    companion object {
        private const val RSA_KEY_ALIAS = "RSA_DEMO"
        private const val RSA_MODE_LESS_THAN_M = "RSA/ECB/PKCS1Padding"
        private const val RSA_ALGORITHM = "RSA"
        private const val AES_ALGORITHM = "AES"
        private const val SHARED_PREFERENCE_NAME = "RSAEncryptedKeysSharedPreferences"
        private const val ENCRYPTED_KEY_NAME = "RSAEncryptedKeysKeyName"
    }

}