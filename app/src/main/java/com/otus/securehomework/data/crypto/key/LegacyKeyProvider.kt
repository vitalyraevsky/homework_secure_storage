package com.otus.securehomework.data.crypto.key

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

@Suppress("DEPRECATION")
class LegacyKeyProvider(
    private val applicationContext: Context
) : KeyProvider {

    private val sharedPreferences by lazy {
        applicationContext.getSharedPreferences(SHARED_PREFS_FILE_NAME, Context.MODE_PRIVATE)
    }

    private val keyStore by lazy {
        KeyStore.getInstance(KEY_PROVIDER).apply {
            load(null)
        }
    }

    override fun getAesKey(): SecretKey {
        return getKeyFromPrefs() ?: generateKey()
    }

    private fun getKeyFromPrefs(): SecretKey? {
        val keyBase64 = sharedPreferences.getString(SHARED_PREFS_KEY, null) ?: return null
        val keyEncrypted = Base64.decode(keyBase64, Base64.DEFAULT)

        val keyDecrypted = rsaDecrypt(keyEncrypted)

        return SecretKeySpec(keyDecrypted, AES_ALGORITHM)
    }

    private fun generateKey(): SecretKey {
        val random = SecureRandom()
        val key = ByteArray(16)

        random.nextBytes(key)

        val encryptedKey = rsaEncrypt(key)
        val encryptedKeyBase64encoded = Base64.encodeToString(encryptedKey, Base64.DEFAULT)

        sharedPreferences.edit {
            putString(SHARED_PREFS_KEY, encryptedKeyBase64encoded)
        }

        return SecretKeySpec(key, AES_ALGORITHM)
    }

    private fun rsaDecrypt(encrypted: ByteArray): ByteArray {
        val privateKey = keyStore.getRsaPrivateKey()
        val cipher = Cipher.getInstance(RSA_MODE_LESS_THAN_M).apply {
            init(Cipher.DECRYPT_MODE, privateKey)
        }

        return cipher.doFinal(encrypted)
    }

    private fun rsaEncrypt(plain: ByteArray): ByteArray {
        val publicKey = keyStore.getRsaPublicKey()
        val cipher = Cipher.getInstance(RSA_MODE_LESS_THAN_M).apply {
            init(Cipher.ENCRYPT_MODE, publicKey)
        }

        return cipher.doFinal(plain)
    }

    private fun KeyStore.getRsaPrivateKey(): PrivateKey {
        return (getKey(RSA_KEY_ALIAS, null) ?: generateKeyPair().private) as PrivateKey
    }

    private fun KeyStore.getRsaPublicKey(): PublicKey {
        return getCertificate(RSA_KEY_ALIAS)?.publicKey ?: generateKeyPair().public
    }

    private fun generateKeyPair(): KeyPair {
        val start = Calendar.getInstance()
        val end = Calendar.getInstance().apply {
            add(Calendar.YEAR, 30)
        }

        val spec = KeyPairGeneratorSpec.Builder(applicationContext)
            .setAlias(RSA_KEY_ALIAS)
            .setSubject(X500Principal("CN=$RSA_KEY_ALIAS"))
            .setSerialNumber(BigInteger.TEN)
            .setStartDate(start.time)
            .setEndDate(end.time)
            .build()

        return KeyPairGenerator.getInstance(RSA_ALGORITHM, KEY_PROVIDER).run {
            initialize(spec)
            generateKeyPair()
        }
    }

    private companion object {
        const val SHARED_PREFS_FILE_NAME = "legacy_key_storage"
        const val SHARED_PREFS_KEY = "aes_key"

        const val KEY_PROVIDER = "AndroidKeyStore"

        const val AES_ALGORITHM = "AES"

        const val RSA_ALGORITHM = "RSA"
        const val RSA_KEY_ALIAS = "RsaKey"
        const val RSA_MODE_LESS_THAN_M = "RSA/ECB/PKCS1Padding"
    }
}