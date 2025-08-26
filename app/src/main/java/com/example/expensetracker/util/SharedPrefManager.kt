package com.example.expensetracker.util

import android.content.Context
import android.content.SharedPreferences
import androidx.security.crypto.EncryptedSharedPreferences
import androidx.security.crypto.MasterKeys

class SharedPrefManager(context: Context) {

    private val sharedPreferences: SharedPreferences

    companion object {
        private const val FILE_NAME = "expense_tracker_prefs"
        private const val KEY_LAST_SYNCED_DATE = "last_synced_date"
        private const val KEY_IS_FIRST_LAUNCH = "is_first_launch"
        private const val KEY_PASSWORD_HASH = "password_hash"
        private const val KEY_PASSWORD_SALT = "password_salt"
    }

    init {
        val masterKeyAlias = MasterKeys.getOrCreate(MasterKeys.AES256_GCM_SPEC)
        sharedPreferences = EncryptedSharedPreferences.create(
            FILE_NAME,
            masterKeyAlias,
            context,
            EncryptedSharedPreferences.PrefKeyEncryptionScheme.AES256_SIV,
            EncryptedSharedPreferences.PrefValueEncryptionScheme.AES256_GCM
        )
    }

    fun isFirstLaunch(): Boolean {
        val isFirst = sharedPreferences.getBoolean(KEY_IS_FIRST_LAUNCH, true)
        if (isFirst) {
            sharedPreferences.edit().putBoolean(KEY_IS_FIRST_LAUNCH, false).apply()
        }
        return isFirst
    }

    fun getLastSyncedDate(): Long {
        return sharedPreferences.getLong(KEY_LAST_SYNCED_DATE, 0L)
    }

    fun setLastSyncedDate(date: Long) {
        sharedPreferences.edit().putLong(KEY_LAST_SYNCED_DATE, date).apply()
    }

    fun setPassword(password: String) {
        val salt = java.security.SecureRandom().generateSeed(16)
        val hash = java.security.MessageDigest.getInstance("SHA-256")
            .digest(salt + password.toByteArray())
        sharedPreferences.edit()
            .putString(KEY_PASSWORD_HASH, android.util.Base64.encodeToString(hash, android.util.Base64.NO_WRAP))
            .putString(KEY_PASSWORD_SALT, android.util.Base64.encodeToString(salt, android.util.Base64.NO_WRAP))
            .apply()
    }

    fun isPasswordCorrect(password: String): Boolean {
        val storedHashString = sharedPreferences.getString(KEY_PASSWORD_HASH, null) ?: return false
        val storedSaltString = sharedPreferences.getString(KEY_PASSWORD_SALT, null) ?: return false

        val salt = android.util.Base64.decode(storedSaltString, android.util.Base64.NO_WRAP)
        val enteredHash = java.security.MessageDigest.getInstance("SHA-256")
            .digest(salt + password.toByteArray())
        val enteredHashString = android.util.Base64.encodeToString(enteredHash, android.util.Base64.NO_WRAP)

        return storedHashString == enteredHashString
    }

    fun isPasswordSet(): Boolean {
        return sharedPreferences.contains(KEY_PASSWORD_HASH)
    }
}
