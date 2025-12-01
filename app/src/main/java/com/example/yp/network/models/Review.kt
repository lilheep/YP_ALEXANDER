package com.example.yp.network.models

import com.google.gson.annotations.SerializedName

data class ReviewResponse(
    val docs: List<Review>,
    val total: Int,
    val limit: Int,
    val page: Int,
    val pages: Int
)

data class Review(
    val id: Int?,
    val movieId: Int?,
    val title: String?,
    val type: String?,
    val review: String?,
    val date: String?,
    val author: String?,
    val userRating: Int?,
    @SerializedName("authorId")
    val authorId: Int?
)