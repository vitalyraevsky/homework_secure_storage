package com.otus.securehomework.security

import android.content.Context
import android.security.KeyPairGeneratorSpec
import android.util.Base64
import androidx.core.content.edit
import java.math.BigInteger
import java.security.*
import java.util.*
import javax.crypto.Cipher
import javax.crypto.SecretKey
import javax.crypto.spec.SecretKeySpec
import javax.security.auth.x500.X500Principal

class LegacyAesKeyProvider(private val applicationContext: Context) : AesKeyProvider {

    private val keyStore by lazy {
        KeyStore.getInstance(KEY_PROVIDER).apply {
            load(null)
        }
    }

    private val sharedPreferences by lazy {
        applicationContext.getSharedPreferences(PREFS_FILE, Context.MODE_PRIVATE)
    }

    override fun getKey(): SecretKey {
        var key = getFromPreferences()

        if (key == null) {
            key = generateKey()
        }

        return key
    }

    private fun getFromPreferences(): SecretKey? {
        val keyBase64 = sharedPreferences.getString(PREFS_KEY, null)

        if (!keyBase64.isNullOrBlank()) {
            val encryptedKey = Base64.decode(keyBase64, Base64.DEFAULT)
            val key = rsaDecryptKey(encryptedKey)

            return SecretKeySpec(key, "AES")
        }

        return null
    }

    private fun rsaDecryptKey(encryptedKey: ByteArray): ByteArray {
        val cipher = Cipher.getInstance(RSA_MODE).apply {
            init(Cipher.DECRYPT_MODE, getRsaPrivateKey())
        }

        return cipher.doFinal(encryptedKey)
    }

    private fun rsaEncryptKey(secret: ByteArray): ByteArray {
        val cipher = Cipher.getInstance(RSA_MODE).apply {
            init(Cipher.ENCRYPT_MODE, getRsaPublicKey())
        }

        return cipher.doFinal(secret)
    }

    private fun getRsaPrivateKey(): PrivateKey {
        return keyStore.getKey(RSA_KEY_ALIAS, null) as? PrivateKey ?: generateRsaSecretKey().private
    }

    private fun generateKey(): SecretKey {
        val key = ByteArray(KEY_LENGTH)

        SecureRandom().run {
            nextBytes(key)
        }

        val encryptedKeyBase64encoded = Base64.encodeToString(rsaEncryptKey(key), Base64.DEFAULT)

        sharedPreferences.edit {
            putString(PREFS_KEY, encryptedKeyBase64encoded)
        }

        return SecretKeySpec(key, "AES")
    }

    private fun getRsaPublicKey(): PublicKey {
        return keyStore.getCertificate(RSA_KEY_ALIAS)?.publicKey ?: generateRsaSecretKey().public
    }

    private fun generateRsaSecretKey(): KeyPair {
        val start: Calendar = Calendar.getInstance()
        val end: Calendar = Calendar.getInstance().apply {
            add(Calendar.YEAR, 30)
        }

        val spec = KeyPairGeneratorSpec.Builder(applicationContext)
            .setAlias(RSA_KEY_ALIAS)
            .setSubject(X500Principal("CN=$RSA_KEY_ALIAS"))
            .setSerialNumber(BigInteger.TEN)
            .setStartDate(start.time)
            .setEndDate(end.time)
            .build()

        return KeyPairGenerator.getInstance(RSA_ALGO, KEY_PROVIDER).run {
            initialize(spec)
            generateKeyPair()
        }
    }

    private companion object {
        const val KEY_PROVIDER = "AndroidKeyStore"
        const val KEY_LENGTH = 16

        const val PREFS_FILE = "key_prefs"
        const val PREFS_KEY = "encrypted_key"

        const val RSA_ALGO = "RSA"
        const val RSA_MODE = "RSA/ECB/PKCS1Padding"
        const val RSA_KEY_ALIAS = "RSA_OTUS"
    }
}