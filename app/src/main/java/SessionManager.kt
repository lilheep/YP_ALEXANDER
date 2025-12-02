package com.example.yp.utils

import android.content.Context
import android.content.SharedPreferences

class SessionManager(context: Context) {
    private val sharedPref: SharedPreferences = context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE)
    private val editor: SharedPreferences.Editor = sharedPref.edit()

    companion object {
        private const val PREF_NAME = "MovieAppPref"
        private const val KEY_USER_ID = "user_id"
        private const val KEY_EMAIL = "email"
        private const val KEY_IS_LOGGED_IN = "is_logged_in"
    }

    fun createSession(userId: Int, email: String) {
        editor.putInt(KEY_USER_ID, userId)
        editor.putString(KEY_EMAIL, email)
        editor.putBoolean(KEY_IS_LOGGED_IN, true)
        editor.apply()
    }

    fun updateEmail(newEmail: String) {
        editor.putString(KEY_EMAIL, newEmail)
        editor.apply()
    }

    fun getUserId(): Int? {
        return if (sharedPref.contains(KEY_USER_ID)) {
            sharedPref.getInt(KEY_USER_ID, -1)
        } else {
            null
        }
    }

    fun getUserEmail(): String? {
        return sharedPref.getString(KEY_EMAIL, null)
    }

    fun isLoggedIn(): Boolean {
        return sharedPref.getBoolean(KEY_IS_LOGGED_IN, false)
    }

    fun logout() {
        editor.clear()
        editor.apply()
    }
}