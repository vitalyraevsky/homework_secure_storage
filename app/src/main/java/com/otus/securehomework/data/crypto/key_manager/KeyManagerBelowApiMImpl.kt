@file:Suppress("DEPRECATION")

package com.otus.securehomework.data.crypto.key_manager

import android.content.Context
import android.security.KeyPairGeneratorSpec
import android.util.Base64
import com.otus.securehomework.data.enums.KeyManagerType
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

    override fun getSecretKey(
        keyName: String,
        type: KeyManagerType
    ): SecretKey {
        return getEncryptedSecretKey(keyName) ?: generateSecretKey(keyName)
    }

    private fun getEncryptedSecretKey(keyName: String): SecretKey? {
        val encryptedSecretKey = getSecretKeyFromSharedPreferences(keyName)
        return encryptedSecretKey?.let {
            val encryptedKey = Base64.decode(encryptedSecretKey, Base64.DEFAULT)
            val key = getDecryptedKey(
                encryptedKey = encryptedKey,
                keyName = keyName
            )
            SecretKeySpec(key, AES_ALGORITHM)
        }
    }

    private fun generateSecretKey(keyName: String): SecretKey {
        val key = ByteArray(BYTE_SIZE)
        SecureRandom().run {
            nextBytes(key)
        }
        val encodedEncryptedKey = Base64.encodeToString(
            getEncryptedKey(
                secret = key,
                keyName = keyName
            ),
            Base64.DEFAULT
        )
        saveSecretKeyToSharedPreferences(
            encryptedKey = encodedEncryptedKey,
            keyName = keyName
        )

        return SecretKeySpec(key, AES_ALGORITHM)
    }

    private fun getDecryptedKey(
        encryptedKey: ByteArray?,
        keyName: String
    ): ByteArray {
        val cipher = Cipher.getInstance(RSA_MODE)
        cipher.init(Cipher.DECRYPT_MODE, getPrivateKey(keyName))
        return cipher.doFinal(encryptedKey)
    }

    private fun getEncryptedKey(
        secret: ByteArray,
        keyName: String
    ): ByteArray {
        val cipher = Cipher.getInstance(RSA_MODE)
        cipher.init(Cipher.ENCRYPT_MODE, getPublicKey(keyName))
        return cipher.doFinal(secret)
    }

    private fun getPrivateKey(keyName: String): PrivateKey {
        return keyStore.getKey(keyName, null) as? PrivateKey ?: createKeyPair(keyName).private
    }

    private fun getPublicKey(keyName: String): PublicKey {
        return keyStore.getCertificate(keyName)?.publicKey ?: createKeyPair(keyName).public
    }

    private fun createKeyPair(keyName: String): KeyPair {
        val generator = KeyPairGenerator.getInstance(RSA_ALGORITHM, ANDROID_KEY_STORE)

        generator.initialize(getKeyPairGeneratorSpec(keyName))
        return generator.generateKeyPair()
    }

    private fun getKeyPairGeneratorSpec(keyName: String): KeyPairGeneratorSpec {
        val startDate = Calendar.getInstance()
        val endDate = Calendar.getInstance().apply {
            add(Calendar.YEAR, 20)
        }

        return KeyPairGeneratorSpec.Builder(context)
            .setAlias(keyName)
            .setSerialNumber(BigInteger.ONE)
            .setSubject(X500Principal("CN=$keyName CA Certificate"))
            .setStartDate(startDate.time)
            .setEndDate(endDate.time)
            .build()
    }

    private fun getSecretKeyFromSharedPreferences(keyName: String): String? {
        return sharedPreferences.getString(getPrefsKey(keyName), null)
    }

    private fun saveSecretKeyToSharedPreferences(encryptedKey: String, keyName: String) {
        sharedPreferences.edit().putString(getPrefsKey(keyName), encryptedKey).apply()
    }

    private fun getPrefsKey(keyName: String): String {
        return keyName + "_$ENCRYPTED_KEY_NAME"
    }

    companion object {
        private const val SHARED_PREFERENCE_NAME = "RSAEncryptorSharedPreferences"

        private const val ENCRYPTED_KEY_NAME = "EncryptedKeyName"
        private const val ANDROID_KEY_STORE = "AndroidKeyStore"
        private const val AES_ALGORITHM = "AES"
        private const val RSA_ALGORITHM = "RSA"
        private const val RSA_MODE = "RSA/ECB/PKCS1Padding"
        private const val BYTE_SIZE = 16
    }

}