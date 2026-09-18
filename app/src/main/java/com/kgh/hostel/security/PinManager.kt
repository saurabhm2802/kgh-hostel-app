package com.kgh.hostel.security

import android.content.Context
import androidx.security.crypto.EncryptedSharedPreferences
import androidx.security.crypto.MasterKey
import dagger.hilt.android.qualifiers.ApplicationContext
import java.security.MessageDigest
import java.security.SecureRandom
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Stores only a salted hash of the admin PIN via EncryptedSharedPreferences
 * (backed by Android Keystore) — the raw PIN is never written to disk.
 */
@Singleton
class PinManager @Inject constructor(
    @ApplicationContext context: Context
) {
    private val masterKey = MasterKey.Builder(context)
        .setKeyScheme(MasterKey.KeyScheme.AES256_GCM)
        .build()

    private val prefs = EncryptedSharedPreferences.create(
        context,
        "kgh_secure_prefs",
        masterKey,
        EncryptedSharedPreferences.PrefKeyEncryptionScheme.AES256_SIV,
        EncryptedSharedPreferences.PrefValueEncryptionScheme.AES256_GCM
    )

    fun isPinSet(): Boolean = prefs.contains("pin_hash")

    fun setPin(pin: String) {
        val salt = ByteArray(16).also { SecureRandom().nextBytes(it) }
        val hash = hash(pin, salt)
        prefs.edit()
            .putString("pin_salt", salt.joinToString(",") { it.toString() })
            .putString("pin_hash", hash)
            .apply()
    }

    fun verifyPin(pin: String): Boolean {
        val saltStr = prefs.getString("pin_salt", null) ?: return false
        val salt = saltStr.split(",").map { it.toByte() }.toByteArray()
        val storedHash = prefs.getString("pin_hash", null) ?: return false
        return hash(pin, salt) == storedHash
    }

    private fun hash(pin: String, salt: ByteArray): String {
        val digest = MessageDigest.getInstance("SHA-256")
        digest.update(salt)
        val bytes = digest.digest(pin.toByteArray())
        return bytes.joinToString("") { "%02x".format(it) }
    }
}
