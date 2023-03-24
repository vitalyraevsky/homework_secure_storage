package com.otus.securehomework.security.token

import android.os.Build
import android.security.keystore.KeyGenParameterSpec
import android.security.keystore.KeyProperties
import android.util.Base64
import androidx.annotation.RequiresApi
import androidx.datastore.preferences.core.Preferences
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.channelFlow
import kotlinx.coroutines.launch
import java.security.KeyStore
import javax.crypto.Cipher
import javax.crypto.KeyGenerator
import javax.crypto.SecretKey
import javax.crypto.spec.IvParameterSpec
import kotlin.coroutines.resume
import kotlin.coroutines.suspendCoroutine


@RequiresApi(Build.VERSION_CODES.M)
class TokenImplM : IToken {

    private val ks: KeyStore by lazy {
        KeyStore.getInstance(KEY_STORE).apply {
            load(null)
        }
    }

    private val ivSpec: IvParameterSpec by lazy {
        IvParameterSpec(
            byteArrayOf(
                11,
                22,
                33,
                44,
                55,
                66,
                77,
                88,
                99,
                100,
                111,
                123,
                123,
                124,
                125
            )
        )
    }

    override suspend fun encryptTokens(
        tokens: Map<Preferences.Key<String>, String?>
    ): Flow<Preferences.Pair<String>> = channelFlow {
        val secretKey = getOrCreateSecretKey()
        tokens.forEach {
            launch {
                val encryptedToken = encrypt(it.value, secretKey)
                send(it.key to encryptedToken)
            }
        }
    }

    override suspend fun decryptToken(token: String): String =
        decrypt(token, getOrCreateSecretKey())

    private suspend fun encrypt(data: String?, secretKey: SecretKey): String = coroutineScope {
        suspendCoroutine {
            launch(Dispatchers.Default) {
                val cipher: Cipher = Cipher.getInstance(AES_MODE)
                cipher.init(Cipher.ENCRYPT_MODE, secretKey, ivSpec)
                val encodedBytes = cipher.doFinal(data?.toByteArray())
                it.resume(Base64.encodeToString(encodedBytes, Base64.NO_WRAP))
            }
        }
    }

    private suspend fun decrypt(encrypted: String, secretKey: SecretKey): String = coroutineScope {
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

    private suspend fun getOrCreateSecretKey(): SecretKey =
        (ks.getEntry(AES_KEY_ALIAS, null) as? KeyStore.SecretKeyEntry)?.secretKey
            ?: generateSecretKey()


    private suspend fun generateSecretKey(): SecretKey = coroutineScope {
        suspendCoroutine {
            launch(Dispatchers.Default) {
                val keyGenerator =
                    KeyGenerator.getInstance(KeyProperties.KEY_ALGORITHM_AES, KEY_STORE)
                val spec = KeyGenParameterSpec.Builder(
                    AES_KEY_ALIAS,
                    KeyProperties.PURPOSE_ENCRYPT or KeyProperties.PURPOSE_DECRYPT
                )
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

    private companion object {
        private const val KEY_STORE = "AndroidKeyStore"
        private const val AES_MODE = "AES/CBC/PKCS7Padding"
        private const val AES_KEY_ALIAS = "TOKENS_CRYPT_AES_KEY"
        private const val AES_KEY_LENGTH = 256
    }
}
