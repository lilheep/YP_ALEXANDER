package com.example.yp.utils

import android.content.Context
import android.content.SharedPreferences

class SessionManager(context: Context) {
    private val sharedPreferences: SharedPreferences =
        context.getSharedPreferences("user_session", Context.MODE_PRIVATE)

    companion object {
        private const val KEY_USER_ID = "user_id"
        private const val KEY_USER_EMAIL = "user_email"
        private const val KEY_IS_LOGGED_IN = "is_logged_in"
    }

    fun createSession(userId: Int, email: String) {
        sharedPreferences.edit().apply {
            putInt(KEY_USER_ID, userId)
            putString(KEY_USER_EMAIL, email)
            putBoolean(KEY_IS_LOGGED_IN, true)
            apply()
        }
    }

    fun getUserId(): Int? {
        return if (sharedPreferences.contains(KEY_USER_ID)) {
            sharedPreferences.getInt(KEY_USER_ID, -1).takeIf { it != -1 }
        } else {
            null
        }
    }

    fun getUserEmail(): String? {
        return sharedPreferences.getString(KEY_USER_EMAIL, null)
    }

    fun isLoggedIn(): Boolean {
        return sharedPreferences.getBoolean(KEY_IS_LOGGED_IN, false) && getUserId() != null
    }

    fun logout() {
        sharedPreferences.edit().apply {
            remove(KEY_USER_ID)
            remove(KEY_USER_EMAIL)
            putBoolean(KEY_IS_LOGGED_IN, false)
            apply()
        }
    }

    fun getCurrentUser(): Pair<Int, String>? {
        return if (isLoggedIn()) {
            val userId = getUserId() ?: return null
            val email = getUserEmail() ?: return null
            Pair(userId, email)
        } else {
            null
        }
    }
}