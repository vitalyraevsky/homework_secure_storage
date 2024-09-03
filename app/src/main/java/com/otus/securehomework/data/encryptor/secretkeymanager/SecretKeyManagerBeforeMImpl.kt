@file:Suppress("DEPRECATION")

package com.otus.securehomework.data.encryptor.secretkeymanager

import android.content.Context
import android.security.KeyPairGeneratorSpec
import android.util.Base64
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.math.BigInteger
import java.security.KeyPair
import java.security.KeyPairGenerator
import java.security.KeyStore
import java.security.PrivateKey
import java.security.SecureRandom
import java.util.Calendar
import javax.crypto.Cipher
import javax.crypto.SecretKey
import javax.crypto.spec.SecretKeySpec
import javax.inject.Inject
import javax.security.auth.x500.X500Principal

class SecretKeyManagerBeforeMImpl @Inject constructor(
    private val context: Context
) : SecretKeyManager {

    companion object {
        private const val SHARED_PREFERENCE_NAME = "RSAEncryptorSharedPreferences"
        private const val ANDROID_KEY_STORE = "AndroidKeyStore"

        private const val AES_ALGORITHM = "AES"
        private const val RSA_ALGORITHM = "RSA"

        private const val RSA_MODE = "RSA/ECB/PKCS1Padding"
        private const val BYTE_SIZE = 16
    }

    private val keyStore by lazy {
        KeyStore.getInstance(ANDROID_KEY_STORE).apply { load(null) }
    }

    private val sharedPreferences by lazy {
        context.getSharedPreferences(SHARED_PREFERENCE_NAME, Context.MODE_PRIVATE)
    }

    override suspend fun getSecretKey(keyName: String): SecretKey =
        getEncryptedSecretKey(keyName) ?: generateSecretKey(keyName)

    private suspend fun getEncryptedSecretKey(keyName: String): SecretKey? =
        withContext(Dispatchers.IO) {
            sharedPreferences.getString(keyName, null)?.let {
                val encryptedKey = Base64.decode(it, Base64.NO_WRAP)
                val key = getDecryptedKey(encryptedKey, keyName)
                SecretKeySpec(key, AES_ALGORITHM)
            }
        }

    private suspend fun getDecryptedKey(encryptedKey: ByteArray?, keyName: String): ByteArray =
        withContext(Dispatchers.IO) {
            Cipher.getInstance(RSA_MODE).run {
                init(Cipher.DECRYPT_MODE, getPrivateKey(keyName))
                doFinal(encryptedKey)
            }
        }

    private suspend fun getPrivateKey(keyName: String) = withContext(Dispatchers.IO) {
        keyStore.getKey(keyName, null) as? PrivateKey ?: createKeyPair(keyName).private
    }

    private suspend fun createKeyPair(keyName: String): KeyPair = withContext(Dispatchers.IO) {
        val keyPairGenerator = KeyPairGenerator.getInstance(RSA_ALGORITHM, ANDROID_KEY_STORE)
        val start = Calendar.getInstance()
        val end = Calendar.getInstance().apply { add(Calendar.YEAR, 20) }
        val spec = KeyPairGeneratorSpec.Builder(context)
            .setAlias(keyName)
            .setSubject(X500Principal("CN=$keyName CA Certificate"))
            .setSerialNumber(BigInteger.ONE)
            .setStartDate(start.time)
            .setEndDate(end.time)
            .build()

        keyPairGenerator.initialize(spec)
        keyPairGenerator.generateKeyPair()
    }


    private suspend fun generateSecretKey(keyName: String): SecretKey =
        withContext(Dispatchers.IO) {
            val key = ByteArray(BYTE_SIZE).apply {
                SecureRandom().nextBytes(this)
            }
            val encryptedKey = getEncryptedKey(key, keyName)
            val encodedEncryptedKey = Base64.encodeToString(encryptedKey, Base64.NO_WRAP)

            sharedPreferences.edit().putString(keyName, encodedEncryptedKey).apply()
            SecretKeySpec(key, AES_ALGORITHM)
        }

    private suspend fun getEncryptedKey(key: ByteArray, keyName: String): ByteArray =
        withContext(Dispatchers.IO) {
            Cipher.getInstance(RSA_MODE).run {
                init(Cipher.ENCRYPT_MODE, getPublicKey(keyName))
                doFinal(key)
            }
        }

    private suspend fun getPublicKey(keyName: String) =
        keyStore.getCertificate(keyName)?.publicKey ?: createKeyPair(keyName).public
}