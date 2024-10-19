package com.testdemo.dsaapp.utils

import android.content.SharedPreferences
import androidx.security.crypto.EncryptedSharedPreferences
import androidx.security.crypto.MasterKeys

class Prefs {
    companion object {
        private const val PREF_NAME = "TrackerApp"
        private lateinit var sharedPreferences: SharedPreferences
        private lateinit var editor: SharedPreferences.Editor
        fun getInstance(): Prefs {
            val masterKey: String = MasterKeys.getOrCreate(MasterKeys.AES256_GCM_SPEC)
            sharedPreferences = EncryptedSharedPreferences.create(PREF_NAME, masterKey,
                BaseApp.getAppContext(),
                EncryptedSharedPreferences.PrefKeyEncryptionScheme.AES256_SIV,
                EncryptedSharedPreferences.PrefValueEncryptionScheme.AES256_GCM)
            editor = sharedPreferences.edit()
            return Prefs()
        }
    }

    fun setPrefsString(key: String, value: String) {
        editor.putString(key, value).apply()
    }

    fun getPrefsString(key: String): String {
        return sharedPreferences.getString(key, "").toString()
    }
    fun setPrefsInt(key: String, value: Int) {
        editor.putInt(key, value).apply()
    }

    fun getPrefsInt(key: String): Int {
        return sharedPreferences.getInt(key, 0)
    }
    fun getPrefsBoolean(key: String): Boolean {
        return sharedPreferences.getBoolean(key, false)
    }

    fun setPrefsBoolean(key: String, value: Boolean) {
        editor.putBoolean(key, value)?.apply()
    }


    fun remove(key: String?) {
        editor.remove(key)?.apply()
    }
    fun logout() {
        editor.clear().apply()
    }
}