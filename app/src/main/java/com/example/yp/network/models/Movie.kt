package com.example.yp.network.models

data class Movie(
    val id: Int,
    val name: String?,
    val alternativeName: String?,
    val year: Int?,
    val description: String?,
    val rating: Rating?,
    val poster: Poster?,
    val countries: List<Country>?,
    val genres: List<Genre>?
)

data class Rating(
    val kp: Double?,
    val imdb: Double?
)

data class Poster(
    val url: String?,
    val previewUrl: String?
)

data class Country(
    val name: String
)

data class Genre(
    val name: String
)

data class MovieResponse(
    val docs: List<Movie>,
    val total: Int,
    val limit: Int,
    val page: Int,
    val pages: Int
)