package com.example.yp.database

import android.content.ContentValues
import android.content.Context
import android.database.sqlite.SQLiteDatabase
import android.database.sqlite.SQLiteOpenHelper
import com.example.yp.HashingPassword

class DBHelper(context: Context, factory: SQLiteDatabase.CursorFactory?) :
    SQLiteOpenHelper(context, DATABASE_NAME, factory, DATABASE_VERSION) {

    override fun onCreate(db: SQLiteDatabase) {
        val createUsersTable = """
            CREATE TABLE $TABLE_USERS (
                $USER_ID INTEGER PRIMARY KEY AUTOINCREMENT,
                $EMAIL TEXT UNIQUE NOT NULL,
                $PASSWORD TEXT NOT NULL,
                $CREATED_AT DATETIME DEFAULT CURRENT_TIMESTAMP
            )
        """.trimIndent()

        db.execSQL(createUsersTable)
    }

    override fun onUpgrade(db: SQLiteDatabase, oldVersion: Int, newVersion: Int) {
        db.execSQL("DROP TABLE IF EXISTS $TABLE_USERS")
        onCreate(db)
    }

    fun getByEmail(email: String): User? {
        val cursor = readableDatabase.query(
            TABLE_USERS,
            arrayOf(USER_ID, EMAIL, PASSWORD),
            "$EMAIL = ?",
            arrayOf(email),
            null, null, null
        )

        return if (cursor.moveToFirst()) {
            val id = cursor.getInt(cursor.getColumnIndexOrThrow(USER_ID))
            val email = cursor.getString(cursor.getColumnIndexOrThrow(EMAIL))
            val password = cursor.getString(cursor.getColumnIndexOrThrow(PASSWORD))
            User(email, password)
        } else {
            null
        }.also { cursor.close() }
    }

    fun save(email: String, password: String): Long {
        val db = writableDatabase
        val values = ContentValues().apply {
            put(EMAIL, email)
            put(PASSWORD, HashingPassword.hashPassword(password))
        }
        val result = db.insert(TABLE_USERS, null, values)
        db.close()
        return result
    }

    companion object {
        private const val DATABASE_NAME = "MoviesGuide.db"
        private const val DATABASE_VERSION = 3

        const val TABLE_USERS = "users"
        const val USER_ID = "user_id"
        const val EMAIL = "email"
        const val PASSWORD = "password"
        const val CREATED_AT = "created_at"
    }
}