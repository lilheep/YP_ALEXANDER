package com.example.yp.database

import android.content.ContentValues
import android.content.Context
import android.database.sqlite.SQLiteDatabase
import android.database.sqlite.SQLiteOpenHelper
import com.example.yp.HashingPassword
import com.example.yp.network.models.FavoriteMovie

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

        val createFavoritesTable = """
            CREATE TABLE $TABLE_FAVORITES (
                $FAVORITE_ID INTEGER PRIMARY KEY AUTOINCREMENT,
                $USER_ID INTEGER NOT NULL,
                $MOVIE_ID INTEGER NOT NULL,
                $MOVIE_TITLE TEXT NOT NULL,
                $MOVIE_POSTER_URL TEXT,
                $MOVIE_YEAR INTEGER,
                $MOVIE_RATING REAL,
                $CREATED_AT DATETIME DEFAULT CURRENT_TIMESTAMP,
                FOREIGN KEY ($USER_ID) REFERENCES $TABLE_USERS($USER_ID) ON DELETE CASCADE,
                UNIQUE($USER_ID, $MOVIE_ID)
            )
        """.trimIndent()

        db.execSQL(createUsersTable)
        db.execSQL(createFavoritesTable)
    }

    override fun onUpgrade(db: SQLiteDatabase, oldVersion: Int, newVersion: Int) {
        if (oldVersion < 4) {
            db.execSQL("DROP TABLE IF EXISTS $TABLE_FAVORITES")
            db.execSQL("DROP TABLE IF EXISTS $TABLE_USERS")
            onCreate(db)
        }
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
            User(id, email, password)
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


    fun addFavorite(
        userId: Int,
        movieId: Int,
        movieTitle: String,
        moviePosterUrl: String?,
        movieYear: Int?,
        movieRating: Double?
    ): Long {
        val db = writableDatabase
        val values = ContentValues().apply {
            put(USER_ID, userId)
            put(MOVIE_ID, movieId)
            put(MOVIE_TITLE, movieTitle)
            put(MOVIE_POSTER_URL, moviePosterUrl ?: "")
            put(MOVIE_YEAR, movieYear ?: 0)
            put(MOVIE_RATING, movieRating ?: 0.0)
        }
        val result = db.insert(TABLE_FAVORITES, null, values)
        db.close()
        return result
    }

    fun removeFavorite(userId: Int, movieId: Int): Boolean {
        val db = writableDatabase
        val result = db.delete(
            TABLE_FAVORITES,
            "$USER_ID = ? AND $MOVIE_ID = ?",
            arrayOf(userId.toString(), movieId.toString())
        )
        db.close()
        return result > 0
    }

    fun isFavorite(userId: Int, movieId: Int): Boolean {
        val db = readableDatabase
        val cursor = db.query(
            TABLE_FAVORITES,
            arrayOf(FAVORITE_ID),
            "$USER_ID = ? AND $MOVIE_ID = ?",
            arrayOf(userId.toString(), movieId.toString()),
            null, null, null
        )
        val exists = cursor.moveToFirst()
        cursor.close()
        db.close()
        return exists
    }

    fun getFavorites(userId: Int): List<FavoriteMovie> {
        val db = readableDatabase
        val cursor = db.query(
            TABLE_FAVORITES,
            arrayOf(FAVORITE_ID, MOVIE_ID, MOVIE_TITLE, MOVIE_POSTER_URL, MOVIE_YEAR, MOVIE_RATING, CREATED_AT),
            "$USER_ID = ?",
            arrayOf(userId.toString()),
            null, null, "$CREATED_AT DESC"
        )

        val favorites = mutableListOf<FavoriteMovie>()
        while (cursor.moveToNext()) {
            favorites.add(
                FavoriteMovie(
                    id = cursor.getInt(cursor.getColumnIndexOrThrow(FAVORITE_ID)),
                    userId = userId,
                    movieId = cursor.getInt(cursor.getColumnIndexOrThrow(MOVIE_ID)),
                    movieTitle = cursor.getString(cursor.getColumnIndexOrThrow(MOVIE_TITLE)),
                    moviePosterUrl = cursor.getString(cursor.getColumnIndexOrThrow(MOVIE_POSTER_URL)),
                    movieYear = cursor.getInt(cursor.getColumnIndexOrThrow(MOVIE_YEAR)),
                    movieRating = cursor.getDouble(cursor.getColumnIndexOrThrow(MOVIE_RATING)),
                    createdAt = cursor.getString(cursor.getColumnIndexOrThrow(CREATED_AT))
                )
            )
        }
        cursor.close()
        db.close()
        return favorites
    }

    companion object {
        private const val DATABASE_NAME = "MoviesGuide.db"
        private const val DATABASE_VERSION = 4
        const val TABLE_USERS = "users"
        const val USER_ID = "user_id"
        const val EMAIL = "email"
        const val PASSWORD = "password"
        const val CREATED_AT = "created_at"

        const val TABLE_FAVORITES = "favorites"
        const val FAVORITE_ID = "favorite_id"
        const val MOVIE_ID = "movie_id"
        const val MOVIE_TITLE = "movie_title"
        const val MOVIE_POSTER_URL = "movie_poster_url"
        const val MOVIE_YEAR = "movie_year"
        const val MOVIE_RATING = "movie_rating"
    }
}