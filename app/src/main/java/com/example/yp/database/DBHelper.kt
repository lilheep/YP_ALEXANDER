package com.example.yp.database

import android.content.ContentValues
import android.content.Context
import android.database.sqlite.SQLiteDatabase
import android.database.sqlite.SQLiteOpenHelper
import com.example.yp.HashingPassword
import com.example.yp.database.User

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
                FOREIGN KEY ($GENRE_ID) REFERENCES $TABLE_GENRES($GENRE_ID) ON DELETE SET NULL,
                UNIQUE($TITLE, $RELEASE_YEAR)
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
                FOREIGN KEY ($USER_ID) REFERENCES $TABLE_USERS($USER_ID) ON DELETE CASCADE,
                UNIQUE($USER_ID, $MOVIE_ID)  -- один пользователь - один отзыв на фильм
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
                UNIQUE($USER_ID, $MOVIE_ID)  -- предотвращение дублирования избранного
            )
        """.trimIndent()

        db.execSQL(createGenresTable)
        db.execSQL(createMoviesTable)
        db.execSQL(createUsersTable)
        db.execSQL(createReviewsTable)
        db.execSQL(createFavoritesTable)

        db.execSQL("PRAGMA foreign_keys = ON")

        addTestData(db)
    }

    override fun onUpgrade(db: SQLiteDatabase, oldVersion: Int, newVersion: Int) {
        db.execSQL("DROP TABLE IF EXISTS $TABLE_FAVORITES")
        db.execSQL("DROP TABLE IF EXISTS $TABLE_REVIEWS")
        db.execSQL("DROP TABLE IF EXISTS $TABLE_MOVIES")
        db.execSQL("DROP TABLE IF EXISTS $TABLE_GENRES")
        db.execSQL("DROP TABLE IF EXISTS $TABLE_USERS")
        onCreate(db)
    }

    override fun onOpen(db: SQLiteDatabase) {
        super.onOpen(db)
        if (!db.isReadOnly) {
            db.execSQL("PRAGMA foreign_keys = ON")
        }
    }

    private fun addTestData(db: SQLiteDatabase) {
        val testGenres = listOf(
            "Драма" to "Эмоциональные истории о жизни людей",
            "Комедия" to "Веселые и смешные фильмы",
            "Боевик" to "Фильмы с динамичными сценами и экшеном",
            "Фантастика" to "Научная фантастика и фэнтези",
            "Ужасы" to "Страшные и напряженные фильмы",
            "Мелодрама" to "Романтические истории о любви",
            "Триллер" to "Напряженные фильмы с неожиданными поворотами",
            "Детектив" to "Расследования преступлений и загадки",
            "Приключения" to "Путешествия и захватывающие события",
            "Мультфильм" to "Анимационные фильмы для всех возрастов"
        )

        testGenres.forEach { (name, description) ->
            val values = ContentValues().apply {
                put(GENRE_NAME, name)
                put(GENRE_DESCRIPTION, description)
            }
            db.insert(TABLE_GENRES, null, values)
        }

        // Добавляем тестовые фильмы
        val testMovies = listOf(
            MovieData(
                "Побег из Шоушенка",
                "Два заключенных на протяжении нескольких лет ищут способ сбежать из тюрьмы.",
                1, // Драма
                1994,
                9.3,
                142,
                "Фрэнк Дарабонт",
                "США"
            ),
            MovieData(
                "Крестный отец",
                "Эпическая история о сицилийской мафиозной семье Корлеоне.",
                1, // Драма
                1972,
                9.2,
                175,
                "Фрэнсис Форд Коппола",
                "США"
            ),
            MovieData(
                "Темный рыцарь",
                "Бэтмен противостоит своему самому опасному врагу - Джокеру.",
                3, // Боевик
                2008,
                9.0,
                152,
                "Кристофер Нолан",
                "США"
            ),
            MovieData(
                "Пираты Карибского моря",
                "Эксцентричный пират Джек Воробей ищет древний клад.",
                2, // Комедия
                2003,
                8.0,
                143,
                "Гор Вербински",
                "США"
            ),
            MovieData(
                "Звездные войны: Эпизод IV",
                "Молодой Люк Скайуокер вступает в борьбу с Галактической Империей.",
                4, // Фантастика
                1977,
                8.6,
                121,
                "Джордж Лукас",
                "США"
            ),
            MovieData(
                "Сияние",
                "Писатель с семьей зимует в отдаленном отеле, где с ним случаются странные вещи.",
                5, // Ужасы
                1980,
                8.4,
                146,
                "Стэнли Кубрик",
                "США"
            ),
            MovieData(
                "Титаник",
                "Молодые люди из разных социальных слоев влюбляются на борту тонущего корабля.",
                6, // Мелодрама
                1997,
                7.9,
                195,
                "Джеймс Кэмерон",
                "США"
            ),
            MovieData(
                "Начало",
                "Воры, которые крадут идеи из подсознания, получают задание внедрить идею.",
                7, // Триллер
                2010,
                8.8,
                148,
                "Кристофер Нолан",
                "США"
            ),
            MovieData(
                "Шерлок Холмс",
                "Знаменитый детектив расследует серию ритуальных убийств.",
                8, // Детектив
                2009,
                7.6,
                128,
                "Гай Ричи",
                "США"
            ),
            MovieData(
                "Властелин колец: Братство кольца",
                "Хоббит Фродо должен уничтожить опасное кольцо в огне Роковой горы.",
                9, // Приключения
                2001,
                8.8,
                178,
                "Питер Джексон",
                "Новая Зеландия"
            )
        )

        testMovies.forEach { movie ->
            val values = ContentValues().apply {
                put(TITLE, movie.title)
                put(DESCRIPTION, movie.description)
                put(GENRE_ID, movie.genreId)
                put(RELEASE_YEAR, movie.releaseYear)
                put(RATING, movie.rating)
                put(DURATION, movie.duration)
                put(DIRECTOR, movie.director)
                put(COUNTRY, movie.country)
            }
            db.insert(TABLE_MOVIES, null, values)
        }
    }

    private data class MovieData(
        val title: String,
        val description: String,
        val genreId: Int,
        val releaseYear: Int,
        val rating: Double,
        val duration: Int,
        val director: String,
        val country: String
    )

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

    fun insertGenre(name: String, description: String? = null): Long {
        val db = writableDatabase
        val values = ContentValues().apply {
            put(GENRE_NAME, name)
            put(GENRE_DESCRIPTION, description)
        }
        return db.insertWithOnConflict(TABLE_GENRES, null, values, SQLiteDatabase.CONFLICT_IGNORE)
    }

    fun getAllGenres(): List<Genre> {
        val genres = mutableListOf<Genre>()
        val cursor = readableDatabase.query(
            TABLE_GENRES,
            arrayOf(GENRE_ID, GENRE_NAME, GENRE_DESCRIPTION),
            null, null, null, null, "$GENRE_NAME ASC"
        )

        while (cursor.moveToNext()) {
            val id = cursor.getInt(cursor.getColumnIndexOrThrow(GENRE_ID))
            val name = cursor.getString(cursor.getColumnIndexOrThrow(GENRE_NAME))
            val description = cursor.getString(cursor.getColumnIndexOrThrow(GENRE_DESCRIPTION))
            genres.add(Genre(id, name, description))
        }
        cursor.close()
        return genres
    }

    fun getExistingGenres(): Set<String> {
        val existingGenres = mutableSetOf<String>()
        val cursor = readableDatabase.query(
            TABLE_GENRES,
            arrayOf(GENRE_NAME),
            null, null, null, null, null
        )

        while (cursor.moveToNext()) {
            val name = cursor.getString(cursor.getColumnIndexOrThrow(GENRE_NAME))
            existingGenres.add(name)
        }
        cursor.close()
        return existingGenres
    }

    fun getAllMovies(): List<Movie> {
        val movies = mutableListOf<Movie>()
        val query = """
            SELECT m.*, g.$GENRE_NAME as genre_name 
            FROM $TABLE_MOVIES m 
            LEFT JOIN $TABLE_GENRES g ON m.$GENRE_ID = g.$GENRE_ID
            ORDER BY m.$TITLE ASC
        """.trimIndent()

        val cursor = readableDatabase.rawQuery(query, null)

        while (cursor.moveToNext()) {
            val movie = Movie(
                id = cursor.getInt(cursor.getColumnIndexOrThrow(MOVIE_ID)),
                title = cursor.getString(cursor.getColumnIndexOrThrow(TITLE)),
                description = cursor.getString(cursor.getColumnIndexOrThrow(DESCRIPTION)),
                genreId = cursor.getInt(cursor.getColumnIndexOrThrow(GENRE_ID)),
                genreName = cursor.getString(cursor.getColumnIndexOrThrow("genre_name")),
                releaseYear = cursor.getInt(cursor.getColumnIndexOrThrow(RELEASE_YEAR)),
                rating = cursor.getDouble(cursor.getColumnIndexOrThrow(RATING)),
                duration = cursor.getInt(cursor.getColumnIndexOrThrow(DURATION)),
                director = cursor.getString(cursor.getColumnIndexOrThrow(DIRECTOR)),
                country = cursor.getString(cursor.getColumnIndexOrThrow(COUNTRY))
            )
            movies.add(movie)
        }
        cursor.close()
        return movies
    }

    fun getMoviesByGenre(genreId: Int): List<Movie> {
        val movies = mutableListOf<Movie>()
        val query = """
            SELECT m.*, g.$GENRE_NAME as genre_name 
            FROM $TABLE_MOVIES m 
            LEFT JOIN $TABLE_GENRES g ON m.$GENRE_ID = g.$GENRE_ID
            WHERE m.$GENRE_ID = ?
            ORDER BY m.$RATING DESC
        """.trimIndent()

        val cursor = readableDatabase.rawQuery(query, arrayOf(genreId.toString()))

        while (cursor.moveToNext()) {
            val movie = Movie(
                id = cursor.getInt(cursor.getColumnIndexOrThrow(MOVIE_ID)),
                title = cursor.getString(cursor.getColumnIndexOrThrow(TITLE)),
                description = cursor.getString(cursor.getColumnIndexOrThrow(DESCRIPTION)),
                genreId = cursor.getInt(cursor.getColumnIndexOrThrow(GENRE_ID)),
                genreName = cursor.getString(cursor.getColumnIndexOrThrow("genre_name")),
                releaseYear = cursor.getInt(cursor.getColumnIndexOrThrow(RELEASE_YEAR)),
                rating = cursor.getDouble(cursor.getColumnIndexOrThrow(RATING)),
                duration = cursor.getInt(cursor.getColumnIndexOrThrow(DURATION)),
                director = cursor.getString(cursor.getColumnIndexOrThrow(DIRECTOR)),
                country = cursor.getString(cursor.getColumnIndexOrThrow(COUNTRY))
            )
            movies.add(movie)
        }
        cursor.close()
        return movies
    }

    companion object {
        private const val DATABASE_NAME = "MoviesGuide.db"
        private const val DATABASE_VERSION = 2  // Увеличили версию из-за изменений схемы

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

data class Genre(
    val id: Int,
    val name: String,
    val description: String? = null
)

data class Movie(
    val id: Int,
    val title: String,
    val description: String?,
    val genreId: Int,
    val genreName: String?,
    val releaseYear: Int,
    val rating: Double,
    val duration: Int,
    val director: String?,
    val country: String?
)