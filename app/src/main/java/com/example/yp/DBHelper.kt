import android.content.ContentValues
import android.content.Context
import android.database.sqlite.SQLiteDatabase
import android.database.sqlite.SQLiteOpenHelper
import com.example.yp.HashingPassword
import com.example.yp.User


class DBHelper(context: Context, factory: SQLiteDatabase.CursorFactory?) :
    SQLiteOpenHelper(context, DATABASE_NAME, factory, DATABASE_VERSION) {

    override fun onCreate(db: SQLiteDatabase) {
        val createGenresTable = """
            CREATE TABLE $TABLE_GENRES (
                $GENRE_ID INTEGER PRIMARY KEY AUTOINCREMENT,
                $GENRE_NAME TEXT UNIQUE NOT NULL,
                $GENRE_DESCRIPTION TEXT
            )
        """.trimIndent()

        val createMoviesTable = """
            CREATE TABLE $TABLE_MOVIES (
                $MOVIE_ID INTEGER PRIMARY KEY AUTOINCREMENT,
                $TITLE TEXT NOT NULL,
                $DESCRIPTION TEXT,
                $GENRE_ID INTEGER,
                $RELEASE_YEAR INTEGER,
                $RATING REAL DEFAULT 0,
                $POSTER_URL TEXT,
                $POSTER_IMAGE BLOB,
                $DURATION INTEGER,
                $DIRECTOR TEXT,
                $COUNTRY TEXT,
                $BUDGET INTEGER,
                FOREIGN KEY ($GENRE_ID) REFERENCES $TABLE_GENRES($GENRE_ID)
            )
        """.trimIndent()

        val createUsersTable = """
            CREATE TABLE $TABLE_USERS (
                $USER_ID INTEGER PRIMARY KEY AUTOINCREMENT,
                $EMAIL TEXT UNIQUE NOT NULL,
                $PASSWORD TEXT NOT NULL,
                $CREATED_AT DATETIME DEFAULT CURRENT_TIMESTAMP
            )
        """.trimIndent()

        val createReviewsTable = """
            CREATE TABLE $TABLE_REVIEWS (
                $REVIEW_ID INTEGER PRIMARY KEY AUTOINCREMENT,
                $MOVIE_ID INTEGER NOT NULL,
                $USER_ID INTEGER NOT NULL,
                $RATING REAL NOT NULL CHECK ($RATING >= 0 AND $RATING <= 10),
                $COMMENT TEXT,
                $CREATED_AT DATETIME DEFAULT CURRENT_TIMESTAMP,
                FOREIGN KEY ($MOVIE_ID) REFERENCES $TABLE_MOVIES($MOVIE_ID) ON DELETE CASCADE,
                FOREIGN KEY ($USER_ID) REFERENCES $TABLE_USERS($USER_ID) ON DELETE CASCADE
            )
        """.trimIndent()

        val createFavoritesTable = """
            CREATE TABLE $TABLE_FAVORITES (
                $FAVORITE_ID INTEGER PRIMARY KEY AUTOINCREMENT,
                $USER_ID INTEGER NOT NULL,
                $MOVIE_ID INTEGER NOT NULL,
                $ADDED_AT DATETIME DEFAULT CURRENT_TIMESTAMP,
                FOREIGN KEY ($USER_ID) REFERENCES $TABLE_USERS($USER_ID) ON DELETE CASCADE,
                FOREIGN KEY ($MOVIE_ID) REFERENCES $TABLE_MOVIES($MOVIE_ID) ON DELETE CASCADE,
                UNIQUE($USER_ID, $MOVIE_ID)
            )
        """.trimIndent()

        db.execSQL(createGenresTable)
        db.execSQL(createMoviesTable)
        db.execSQL(createUsersTable)
        db.execSQL(createReviewsTable)
        db.execSQL(createFavoritesTable)

        db.execSQL("PRAGMA foreign_keys = ON")
    }

    override fun onUpgrade(db: SQLiteDatabase, oldVersion: Int, newVersion: Int) {
        db.execSQL("DROP TABLE IF EXISTS $TABLE_USERS")
        onCreate(db)
    }

    fun getByEmail(email: String): User? {
        val cursor =
            readableDatabase.rawQuery("SELECT * FROM users WHERE email = ?;", arrayOf(email))
        if (cursor.moveToFirst()) {
            val id = cursor.getInt(cursor.getColumnIndex("user_id"))
            val email = cursor.getString(cursor.getColumnIndex("email"))
            val password = cursor.getString(cursor.getColumnIndex("password"))
            return User(email, password)
        }
        cursor.close()
        return null
    }

    fun save(email: String, password: String){
        val db = writableDatabase
        val values = ContentValues().apply {
            put(EMAIL, email)
            put(PASSWORD, HashingPassword.hashPassword(password))
        }
        db.insert(TABLE_USERS, null, values)
        db.close()
    }

    companion object {
        private const val DATABASE_NAME = "MoviesGuide.db"
        private const val DATABASE_VERSION = 1

        // Названия таблиц
        const val TABLE_GENRES = "genres"
        const val TABLE_MOVIES = "movies"
        const val TABLE_USERS = "users"
        const val TABLE_REVIEWS = "reviews"
        const val TABLE_FAVORITES = "favorites"

        // Колонки для жанров
        const val GENRE_ID = "genre_id"
        const val GENRE_NAME = "name"
        const val GENRE_DESCRIPTION = "description"

        // Колонки для фильмов
        const val MOVIE_ID = "movie_id"
        const val TITLE = "title"
        const val DESCRIPTION = "description"
        const val RELEASE_YEAR = "release_year"
        const val RATING = "rating"
        const val POSTER_URL = "poster_url"
        const val POSTER_IMAGE = "poster_image"
        const val DURATION = "duration"
        const val DIRECTOR = "director"
        const val COUNTRY = "country"
        const val BUDGET = "budget"

        // Колонки для пользователей
        const val USER_ID = "user_id"
        const val PASSWORD = "password"
        const val EMAIL = "email"
        const val CREATED_AT = "created_at"

        // Колонки для рецензий
        const val REVIEW_ID = "review_id"
        const val COMMENT = "comment"

        // Колонки для избранного
        const val FAVORITE_ID = "favorite_id"
        const val ADDED_AT = "added_at"
    }
}