package com.otus.securehomework.data.crypto

import android.content.Context
import android.security.KeyPairGeneratorSpec
import android.util.Base64
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.firstOrNull
import kotlinx.coroutines.flow.map
import java.math.BigInteger
import java.security.KeyPairGenerator
import java.security.KeyStore
import java.security.PrivateKey
import java.security.SecureRandom
import java.util.*
import javax.crypto.Cipher
import javax.crypto.SecretKey
import javax.crypto.spec.IvParameterSpec
import javax.crypto.spec.SecretKeySpec
import javax.inject.Inject
import javax.security.auth.x500.X500Principal

class EncryptionLessThanM @Inject constructor(
    private val context: Context,
    private val dataStore: DataStore<Preferences>,
    private val keyStore: KeyStore
) : Encryption {

    private fun String.toByteArrayNoWrap(): ByteArray = Base64.decode(this, Base64.NO_WRAP)
    private fun ByteArray.toStringNoWrap(): String = Base64.encodeToString(this, Base64.NO_WRAP)

    override suspend fun encrypt(bytes: ByteArray?): EncryptionResult {
        val cipher = encryptAesCipher()
        val encryptedBytes = bytes?.let { cipher.doFinal(bytes) } ?: byteArrayOf()
        return EncryptionResult(bytes = encryptedBytes, iv = cipher.iv)
    }

    override suspend fun decrypt(encryptedBytes: ByteArray?, iv: ByteArray) =
        encryptedBytes?.let {
            decryptAesCipher(iv).doFinal(it)
        } ?: byteArrayOf()

    private suspend fun encryptAesCipher(): Cipher = Cipher.getInstance(AES_TRANSFORMATION).apply {
        init(Cipher.ENCRYPT_MODE, getAesKey(), IvParameterSpec(randomIv()))
    }

    private suspend fun decryptAesCipher(iv: ByteArray): Cipher =
        Cipher.getInstance(AES_TRANSFORMATION).apply {
            init(Cipher.DECRYPT_MODE, getAesKey(), IvParameterSpec(iv))
        }

    private fun randomIv() = ByteArray(IV_SIZE).also {
        SecureRandom().nextBytes(it)
    }


    private fun encryptRsaCipher() = Cipher.getInstance(RSA_TRANSFORMATION).apply {
        init(Cipher.ENCRYPT_MODE, getRsaPublicKey())
    }

    private fun decryptRsaCipher() = Cipher.getInstance(RSA_TRANSFORMATION).apply {
        init(Cipher.DECRYPT_MODE, getRsaPrivateKey())
    }

    private val aesKeyFromDataStore: Flow<ByteArray?>
        get() = dataStore.data.map { preferences ->
            val encryptedKey = preferences[AES_KEY_DATA_STORE]?.toByteArrayNoWrap()
            encryptedKey?.let {
                rsaDecryptKey(it)
            }
        }

    private suspend fun getAesKey(): SecretKey {
        val encryptedKey = aesKeyFromDataStore.firstOrNull()
        return encryptedKey?.let {
            SecretKeySpec(it, KEY_ALGORITHM_AES)
        } ?: generateAndSaveAesKey()
    }

    private suspend fun generateAndSaveAesKey(): SecretKey {
        val key = ByteArray(KEY_SIZE)
        SecureRandom().nextBytes(key)
        dataStore.edit { prefs ->
            prefs[AES_KEY_DATA_STORE] = rsaEncryptKey(key).toStringNoWrap()
        }
        return SecretKeySpec(key, KEY_ALGORITHM_AES)
    }

    private fun rsaEncryptKey(key: ByteArray): ByteArray = encryptRsaCipher().doFinal(key)

    private fun rsaDecryptKey(encryptedKey: ByteArray?): ByteArray =
        decryptRsaCipher().doFinal(encryptedKey)

    private fun getRsaPrivateKey() =
        keyStore.getKey(RSA_KEY_KEY_STORE, null) as? PrivateKey ?: generateRsaKeys().private

    private fun getRsaPublicKey() =
        keyStore.getCertificate(RSA_KEY_KEY_STORE)?.publicKey ?: generateRsaKeys().public

    private fun generateRsaKeys() =
        KeyPairGenerator.getInstance(KEY_ALGORITHM_RSA, keyStore.type).apply {
            val start: Calendar = Calendar.getInstance()
            val end: Calendar = Calendar.getInstance()
            end.add(Calendar.YEAR, 30)
            initialize(
                KeyPairGeneratorSpec.Builder(context)
                    .setAlias(RSA_KEY_KEY_STORE)
                    .setSubject(X500Principal("CN=$RSA_KEY_KEY_STORE"))
                    .setSerialNumber(BigInteger.TEN)
                    .setStartDate(start.getTime())
                    .setEndDate(end.getTime())
                    .build()
            )
        }.generateKeyPair()

    companion object {

        private const val KEY_ALGORITHM_AES = "AES"
        private const val BLOCK_MODE_CBC = "CBC"
        private const val PADDING_PKCS5 = "PKCS5Padding"

        private const val KEY_ALGORITHM_RSA = "RSA"
        private const val BLOCK_MODE_ECB = "ECB"
        private const val PADDING_PKCS1 = "PKCS1Padding"

        private const val AES_TRANSFORMATION = "$KEY_ALGORITHM_AES/$BLOCK_MODE_CBC/$PADDING_PKCS5"
        private const val RSA_TRANSFORMATION = "$KEY_ALGORITHM_RSA/$BLOCK_MODE_ECB/$PADDING_PKCS1"

        private const val RSA_KEY_KEY_STORE = "RSA_KEY_KEY_STORE"

        private val AES_KEY_DATA_STORE = stringPreferencesKey("AES_KEY_DATA_STORE")

        private const val KEY_SIZE = 16
        private const val IV_SIZE = 16
    }
}

