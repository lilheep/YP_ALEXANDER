package com.example.yp.network.models

data class FavoriteMovie(
    val id: Int,
    val userId: Int,
    val movieId: Int,
    val movieTitle: String,
    val moviePosterUrl: String,
    val movieYear: Int,
    val movieRating: Double,
    val createdAt: String
)