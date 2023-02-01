package com.otus.securehomework.security

import android.content.Context
import android.util.Base64
import androidx.datastore.preferences.core.Preferences
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.channelFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.security.Key
import java.security.KeyPair
import java.security.KeyPairGenerator
import java.security.KeyStore
import java.security.PrivateKey
import java.security.PublicKey
import javax.crypto.Cipher
import javax.crypto.KeyGenerator
import javax.crypto.SecretKey
import javax.crypto.spec.IvParameterSpec
import javax.crypto.spec.SecretKeySpec
import javax.inject.Inject
import kotlin.coroutines.resume
import kotlin.coroutines.suspendCoroutine

private const val KEY_PROVIDER = "AndroidKeyStore"
private const val PREFS_FILE_NAME = "aesKeyPrefs"
private const val ENCRYPTED_SECRET_KEY = "aesSecretKey"

private const val RSA_ALGORITHM = "RSA"
private const val RSA_MODE = "RSA/ECB/PKCS1Padding"
private const val RSA_KEY_LENGTH = 4096
private const val RSA_KEY_ALIAS = "TOKENS_CRYPT_RSA_KEY"

private const val AES_ALGORITHM = "AES"
private const val AES_MODE = "AES/CBC/PKCS7Padding"
private const val AES_KEY_LENGTH = 256


class TokenCipherBeforeMImpl @Inject constructor(
    private val applicationContext: Context
) : TokenCipher {

    private val ks: KeyStore by lazy {
        KeyStore.getInstance(KEY_PROVIDER).apply {
            load(null)
        }
    }

    private val ivSpec: IvParameterSpec by lazy {
        val initVector: ByteArray =
            byteArrayOf(44, 46, 24, 56, 4, 47, -7, 73, 10, 111, -87, 37, 4, 88, 2, 101)
        IvParameterSpec(initVector)
    }

    override suspend fun encryptTokens(
        tokens: Map<Preferences.Key<String>, String?>
    ): Flow<Preferences.Pair<String>> = channelFlow<Preferences.Pair<String>> {
        val secretKey = getOrCreateAESSecretKey()
        tokens.forEach {
            launch {
                val plainTokenBytes: ByteArray = it.value!!.toByteArray()
                val encryptedTokenBytes = aesEncrypt(plainTokenBytes, secretKey)
                val encodedTokenBytes = Base64.encodeToString(encryptedTokenBytes, Base64.NO_WRAP)
                send(it.key to encodedTokenBytes)
            }
        }
    }

    override suspend fun decryptToken(token: String): String {
        val secretKey = getOrCreateAESSecretKey()
        val encryptedTokenBytes: ByteArray = Base64.decode(token, Base64.NO_WRAP)
        val decryptedTokenBytes: ByteArray = aesDecrypt(encryptedTokenBytes, secretKey)
        return String(decryptedTokenBytes, Charsets.UTF_8)
    }

    private suspend fun rsaEncrypt(
        plainBytes: ByteArray,
        publicKey: PublicKey
    ): ByteArray {
        return baseCrypt(Cipher.ENCRYPT_MODE, RSA_MODE, publicKey, plainBytes)
    }

    private suspend fun rsaDecrypt(
        cipherBytes: ByteArray,
        privateKey: PrivateKey
    ): ByteArray {
        return baseCrypt(Cipher.DECRYPT_MODE, RSA_MODE, privateKey, cipherBytes)
    }

    private suspend fun aesEncrypt(
        plainBytes: ByteArray,
        secretKey: SecretKey
    ): ByteArray {
        return baseCrypt(Cipher.ENCRYPT_MODE, AES_MODE, secretKey, plainBytes)
    }

    private suspend fun aesDecrypt(
        cipherBytes: ByteArray,
        secretKey: SecretKey
    ): ByteArray {
        return baseCrypt(Cipher.DECRYPT_MODE, AES_MODE, secretKey, cipherBytes)
    }

    private suspend fun baseCrypt(
        operation: Int,
        mode: String,
        key: Key,
        bytes: ByteArray
    ): ByteArray {
        return coroutineScope {
            suspendCoroutine {
                launch(Dispatchers.Default) {
                    val cipher: Cipher = Cipher.getInstance(mode)
                    with (cipher) {
                        if (mode == AES_MODE) init(operation, key, ivSpec)
                        else init(operation, key)
                        it.resume(doFinal(bytes))
                    }
                }
            }
        }
    }

    private suspend fun getRSAPrivateKey(): PrivateKey {
        return getRSAPrivateKeyFromKeyStore() ?: run {
            generateRSAKeyPair()
            getRSAPrivateKeyFromKeyStore()!!
        }
    }

    private suspend fun getRSAPublicKey(): PublicKey {
        return getRSAPublicKeyFromKeyStore() ?: run {
            generateRSAKeyPair()
            getRSAPublicKeyFromKeyStore()!!
        }
    }

    private fun getRSAPrivateKeyFromKeyStore(): PrivateKey? {
        return ks.getKey(RSA_KEY_ALIAS, null) as? PrivateKey?
    }

    private fun getRSAPublicKeyFromKeyStore(): PublicKey? {
        return ks.getCertificate(RSA_KEY_ALIAS)?.publicKey
    }

    private suspend fun getOrCreateAESSecretKey(): SecretKey {
        val encryptedSecretKey: String? = getAESSecretKeyFromSharedPrefs()
        return if (encryptedSecretKey != null) {
            val encryptedAESSecretKeyBytes = Base64.decode(encryptedSecretKey, Base64.NO_WRAP)
            val decryptedAESSecretKeyBytes = rsaDecrypt(encryptedAESSecretKeyBytes, getRSAPrivateKey())
            SecretKeySpec(decryptedAESSecretKeyBytes, AES_ALGORITHM)
        } else {
            generateAESSecretKey().also {
                val encryptedAESSecretKeyBytes = rsaEncrypt(it.encoded, getRSAPublicKey())
                val encryptedAESSecretKey =
                    Base64.encodeToString(encryptedAESSecretKeyBytes, Base64.NO_WRAP)
                saveAESSecretKeyToSharedPrefs(encryptedAESSecretKey)
            }
        }
    }

    private suspend fun saveAESSecretKeyToSharedPrefs(secretKey: String) {
        withContext(Dispatchers.IO) {
            val preferences = applicationContext.getSharedPreferences(
                PREFS_FILE_NAME,
                Context.MODE_PRIVATE
            )
            with(preferences.edit()) {
                putString(ENCRYPTED_SECRET_KEY ,secretKey)
                apply()
            }
        }
    }

    private suspend fun getAESSecretKeyFromSharedPrefs(): String? {
        return withContext(Dispatchers.IO) {
            val preferences = applicationContext.getSharedPreferences(
                PREFS_FILE_NAME,
                Context.MODE_PRIVATE
            )
            with(preferences) {
                getString(ENCRYPTED_SECRET_KEY, null)
            }
        }
    }

    private suspend fun generateAESSecretKey(): SecretKey {
        return coroutineScope {
            suspendCoroutine {
                launch(Dispatchers.Default) {
                    val keyGenerator = KeyGenerator.getInstance(AES_ALGORITHM)
                    keyGenerator.init(AES_KEY_LENGTH)
                    it.resume(keyGenerator.generateKey())
                }
            }
        }
    }

    private suspend fun generateRSAKeyPair(): KeyPair {
        return coroutineScope {
            suspendCoroutine {
                launch(Dispatchers.Default) {
                    val startDate = java.util.Calendar.getInstance().time
                    val endCalendar = java.util.Calendar.getInstance()
                    endCalendar.add(java.util.Calendar.YEAR, 10)
                    val endDate = endCalendar.time
                    val kpgSpec = android.security.KeyPairGeneratorSpec.Builder(applicationContext)
                        .setAlias(RSA_KEY_ALIAS)
                        .setKeySize(RSA_KEY_LENGTH)
                        .setSubject(javax.security.auth.x500.X500Principal("CN=$RSA_KEY_ALIAS"))
                        .setSerialNumber(java.math.BigInteger.ONE)
                        .setStartDate(startDate)
                        .setEndDate(endDate)
                        .build()
                    val keyPairGenerator = KeyPairGenerator.getInstance(RSA_ALGORITHM, KEY_PROVIDER)
                    keyPairGenerator.initialize(kpgSpec)
                    it.resume(keyPairGenerator.generateKeyPair())
                }
            }
        }
    }
}
