package com.otus.securehomework.data.crypto

import android.content.Context
import android.security.KeyPairGeneratorSpec
import android.util.Base64
import dagger.hilt.android.qualifiers.ApplicationContext
import java.math.BigInteger
import java.security.*
import java.util.*
import javax.crypto.Cipher
import javax.crypto.SecretKey
import javax.crypto.spec.SecretKeySpec
import javax.security.auth.x500.X500Principal

class BeforeMSecretKey(
    @ApplicationContext private val context: Context
) : ISecretKey {

    private val keyStore by lazy {
        KeyStore.getInstance(ISecretKey.ANDROID_KEY_STORE).apply {
            load(null)
        }
    }

    private val prefs by lazy {
        context.getSharedPreferences(SHARED_PREFERENCE_NAME, Context.MODE_PRIVATE)
    }

    override fun getSecretKey(): SecretKey = getSecretKeyFromSharedPreferences()?.let {

        val encryptedKey = Base64.decode(it, Base64.DEFAULT)
        val key = rsaDecryptKey(encryptedKey)

        SecretKeySpec(key, AES_ALGORITHM)
    } ?: generateAesKey()


    private fun getSecretKeyFromSharedPreferences() =
        prefs.getString(ENCRYPTED_KEY_NAME, null)

    private fun rsaDecryptKey(encryptedKey: ByteArray): ByteArray? =
        Cipher.getInstance(RSA_MODE_LESS_THAN_M).apply {
            init(Cipher.DECRYPT_MODE, getRsaPrivateKey())
        }.doFinal(encryptedKey)

    private fun getRsaPrivateKey(): PrivateKey {
        return keyStore.getKey(RSA_KEY_ALIAS, null) as? PrivateKey
            ?: generateRsaSecretKey().private
    }

    private fun generateAesKey(): SecretKey {
        val key = ByteArray(16)
        SecureRandom().run { nextBytes(key) }

        val encryptedKey = rsaEncryptKey(key)
        val encryptedKeyBase64encoded = Base64.encodeToString(encryptedKey, Base64.DEFAULT)

        prefs.edit().apply {
            putString(ENCRYPTED_KEY_NAME, encryptedKeyBase64encoded)
            apply()
        }

        return SecretKeySpec(key, AES_ALGORITHM)
    }

    private fun rsaEncryptKey(secret: ByteArray): ByteArray =
        Cipher.getInstance(RSA_MODE_LESS_THAN_M).apply {
            init(Cipher.ENCRYPT_MODE, getRsaPublicKey())
        }.doFinal(secret)

    private fun getRsaPublicKey(): PublicKey =
        keyStore.getCertificate(RSA_KEY_ALIAS)?.publicKey ?: generateRsaSecretKey().public

    private fun generateRsaSecretKey(): KeyPair {
        val start: Calendar = Calendar.getInstance()
        val end: Calendar = Calendar.getInstance()
        end.add(Calendar.YEAR, 30)
        val spec = KeyPairGeneratorSpec.Builder(context)
            .setAlias(RSA_KEY_ALIAS)
            .setSubject(X500Principal("CN=$RSA_KEY_ALIAS"))
            .setSerialNumber(BigInteger.TEN)
            .setStartDate(start.time)
            .setEndDate(end.time)
            .build()

        return KeyPairGenerator.getInstance(RSA_ALGORITHM, ISecretKey.ANDROID_KEY_STORE).run {
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