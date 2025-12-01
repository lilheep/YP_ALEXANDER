package com.example.yp.network.models

import com.google.gson.annotations.SerializedName

data class MovieResponse(
    val docs: List<Movie>,
    val total: Int,
    val limit: Int,
    val page: Int,
    val pages: Int
)

data class Movie(
    val id: Int?,
    val name: String?,
    val alternativeName: String?,
    val description: String?,
    val shortDescription: String?,
    val year: Int?,
    val rating: Rating?,
    val poster: Poster?,
    val backdrop: Backdrop?,
    val countries: List<Country>?,
    val genres: List<Genre>?,
    val persons: List<Person>?,
    val movieLength: Int?,
    val ageRating: Int?,
    val budget: Budget?,
    val fees: Fees?,
    val premiere: Premiere?,
    val similarMovies: List<SimilarMovie>?,
    val sequelsAndPrequels: List<SequelPrequel>?,
    val watchability: Watchability?,
    val top10: Int?,
    val top250: Int?,
    val type: String?
)

data class Rating(
    val kp: Double?,
    val imdb: Double?,
    val filmCritics: Double?,
    val russianFilmCritics: Double?,
    val await: Double?
)

data class Poster(
    val url: String?,
    val previewUrl: String?
)

data class Backdrop(
    val url: String?,
    val previewUrl: String?
)

data class Country(
    val name: String?
)

data class Genre(
    val name: String?
)

data class Person(
    val id: Int?,
    val name: String?,
    val photo: String?,
    val profession: String?,
    val description: String?
)

data class Budget(
    val value: Long?,
    val currency: String?
)

data class Fees(
    val world: Money?,
    val russia: Money?,
    val usa: Money?
)

data class Money(
    val value: Long?,
    val currency: String?
)

data class Premiere(
    val world: String?,
    val russia: String?
)

data class SimilarMovie(
    val id: Int?,
    val name: String?,
    val poster: Poster?
)

data class SequelPrequel(
    val id: Int?,
    val name: String?,
    val type: String?
)

data class Watchability(
    val items: List<WatchabilityItem>?
)

data class WatchabilityItem(
    val name: String?,
    val url: String?
)