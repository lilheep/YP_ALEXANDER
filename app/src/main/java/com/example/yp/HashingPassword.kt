package com.example.yp

import at.favre.lib.crypto.bcrypt.BCrypt

object HashingPassword {

    fun hashPassword(password: String): String {
        return BCrypt.withDefaults().hashToString(12, password.toCharArray())
    }

    fun verifyPassword(inputPassword: String, storedHash: String): Boolean {
        return BCrypt.verifyer().verify(inputPassword.toCharArray(), storedHash).verified
    }
}