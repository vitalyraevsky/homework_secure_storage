package com.otus.securehomework.data.security

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

class BeforeMSecretKeyGenerator @Inject constructor(
    private val context: Context,
) : ISecretKeyGenerator {

    private companion object {
        const val ANDROID_KEY_STORE = "AndroidKeyStore"
        const val KEY_ALIAS = "RSA_DEMO"
        const val RSA_MODE = "RSA/ECB/PKCS1Padding"
        const val KEY_ALGORITHM_RSA = "RSA"
        const val KEY_ALGORITHM_AES = "AES"
        const val KEY_SHARED_PREFERENCE = "KeySharedPreferences"
        const val KEY_NAME = "KeyName"
        const val KEY_SIZE = 16
        const val THIRTY = 30
    }

    private val keyStore by lazy {
        KeyStore.getInstance(ANDROID_KEY_STORE).apply { load(null) }
    }

    private val prefs by lazy {
        context.getSharedPreferences(KEY_SHARED_PREFERENCE, Context.MODE_PRIVATE)
    }

    private val cipher by lazy { Cipher.getInstance(RSA_MODE) }

    override fun generateKey(): SecretKey {
        val secretKey = getSecretKeyFromSharedPreferences() ?: return generateAesKey()

        val encryptedKey = Base64.decode(secretKey, Base64.DEFAULT)
        val key = rsaDecryptKey(encryptedKey)

        return SecretKeySpec(key, KEY_ALGORITHM_AES)
    }

    private fun getSecretKeyFromSharedPreferences(): String? {
        return prefs.getString(KEY_NAME, null)
    }

    private fun generateAesKey(): SecretKey {
        val key = ByteArray(KEY_SIZE).apply {
            SecureRandom().nextBytes(this)
        }

        val encryptedKeyBase64encoded = Base64.encodeToString(rsaEncryptKey(key), Base64.DEFAULT)

        prefs.edit().apply {
            putString(KEY_NAME, encryptedKeyBase64encoded)
            apply()
        }

        return SecretKeySpec(key, KEY_ALGORITHM_AES)
    }

    private fun rsaEncryptKey(secret: ByteArray): ByteArray {
        return cipher.run {
            init(Cipher.ENCRYPT_MODE, getRsaPublicKey())
            doFinal(secret)
        }
    }

    private fun rsaDecryptKey(encryptedKey: ByteArray): ByteArray? {
        return cipher.run {
            init(Cipher.DECRYPT_MODE, getRsaPrivateKey())
            doFinal(encryptedKey)
        }
    }

    private fun getRsaPublicKey(): PublicKey {
        val publicKey = keyStore.getCertificate(KEY_ALIAS)?.publicKey
        return publicKey ?: generateRsaSecretKey().public
    }

    private fun getRsaPrivateKey(): PrivateKey {
        val privateKey = keyStore.getKey(KEY_ALIAS, null) as? PrivateKey
        return privateKey ?: generateRsaSecretKey().private
    }

    private fun generateRsaSecretKey(): KeyPair {
        val start: Calendar = Calendar.getInstance()
        val end = Calendar.getInstance().plusYears(THIRTY)

        val spec = KeyPairGeneratorSpec.Builder(context)
            .setAlias(KEY_ALIAS)
            .setSubject(X500Principal("CN=$KEY_ALIAS"))
            .setSerialNumber(BigInteger.TEN)
            .setStartDate(start.time)
            .setEndDate(end.time)
            .build()

        return KeyPairGenerator.getInstance(KEY_ALGORITHM_RSA, ANDROID_KEY_STORE).run {
            initialize(spec)
            generateKeyPair()
        }
    }

    private fun Calendar.plusYears(years: Int): Calendar {
        return apply { add(Calendar.YEAR, years) }
    }
}