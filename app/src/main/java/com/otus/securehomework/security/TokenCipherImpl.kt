package com.otus.securehomework.security

import android.os.Build
import android.security.keystore.KeyGenParameterSpec
import android.security.keystore.KeyProperties
import android.util.Base64
import androidx.annotation.RequiresApi
import androidx.datastore.preferences.core.Preferences
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.channelFlow
import kotlinx.coroutines.launch
import java.security.KeyStore
import java.security.SecureRandom
import javax.crypto.Cipher
import javax.crypto.KeyGenerator
import javax.crypto.SecretKey
import javax.crypto.spec.IvParameterSpec
import kotlin.coroutines.resume
import kotlin.coroutines.suspendCoroutine

private const val KEY_PROVIDER = "AndroidKeyStore"

private const val AES_MODE = "AES/CBC/PKCS7Padding"
private const val AES_KEY_ALIAS = "TOKENS_CRYPT_AES_KEY"
private const val AES_KEY_LENGTH = 256


@RequiresApi(Build.VERSION_CODES.M)
class TokenCipherImpl : TokenCipher {

    private val ks: KeyStore by lazy {
        KeyStore.getInstance(KEY_PROVIDER).apply {
            load(null)
        }
    }

    private val ivSpec: IvParameterSpec by lazy {
        val initVector: ByteArray =
            byteArrayOf(34, 57, 13, 56, 23, 67, 45, 73, 0, 123, -34, 56, 84, 88, 90, 11)
        IvParameterSpec(initVector)
    }

    override suspend fun encryptTokens(
        tokens: Map<Preferences.Key<String>, String?>
    ): Flow<Preferences.Pair<String>> = channelFlow<Preferences.Pair<String>> {
        val secretKey = getOrCreateSecretKey()
        tokens.forEach {
            launch {
                val encryptedToken = encrypt(it.value, secretKey)
                send(it.key to encryptedToken)
            }
        }
    }

    override suspend fun decryptToken(token: String): String {
        val secretKey = getOrCreateSecretKey()
        return decrypt(token, secretKey)
    }

    private suspend fun encrypt(data: String?, secretKey: SecretKey): String {
        return coroutineScope {
            suspendCoroutine {
                launch(Dispatchers.Default) {
                    val cipher: Cipher = Cipher.getInstance(AES_MODE)
                    cipher.init(Cipher.ENCRYPT_MODE, secretKey, ivSpec)
                    val encodedBytes = cipher.doFinal(data?.toByteArray())
                    it.resume(Base64.encodeToString(encodedBytes, Base64.NO_WRAP))
                }
            }
        }
    }

    private suspend fun decrypt(encrypted: String, secretKey: SecretKey): String {
        return coroutineScope {
            suspendCoroutine {
                launch(Dispatchers.Default) {
                    val cipher: Cipher = Cipher.getInstance(AES_MODE)
                    cipher.init(Cipher.DECRYPT_MODE, secretKey, ivSpec)
                    val encodedBytes = Base64.decode(encrypted, Base64.NO_WRAP)
                    val decoded = cipher.doFinal(encodedBytes)
                    it.resume(String(decoded, Charsets.UTF_8))
                }
            }
        }
    }

    private suspend fun getOrCreateSecretKey(): SecretKey {
        val entry = ks.getEntry(AES_KEY_ALIAS, null) as? KeyStore.SecretKeyEntry
        return entry?.secretKey ?: generateSecretKey()
    }

    private suspend fun generateSecretKey(): SecretKey {
        return coroutineScope {
            suspendCoroutine {
                launch(Dispatchers.Default) {
                    val keyGenerator = KeyGenerator.getInstance(KeyProperties.KEY_ALGORITHM_AES, KEY_PROVIDER)
                    val spec = KeyGenParameterSpec.Builder(
                        AES_KEY_ALIAS, KeyProperties.PURPOSE_ENCRYPT or KeyProperties.PURPOSE_DECRYPT)
                        .setBlockModes(KeyProperties.BLOCK_MODE_CBC)
                        .setKeySize(AES_KEY_LENGTH)
                        .setEncryptionPaddings(KeyProperties.ENCRYPTION_PADDING_PKCS7)
                        .setRandomizedEncryptionRequired(false)
                        .build()
                    keyGenerator.init(spec)
                    it.resume(keyGenerator.generateKey())
                }
            }
        }
    }

    /*fun getInitVector(): ByteArray {
        val iv = ByteArray(16)
        SecureRandom().nextBytes(iv)
        return iv
    }*/

}
