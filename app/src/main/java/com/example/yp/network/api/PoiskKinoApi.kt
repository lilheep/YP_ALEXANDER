package com.example.yp.network.api

import com.example.yp.network.models.Movie
import com.example.yp.network.models.MovieResponse
import com.example.yp.network.models.ReviewResponse
import retrofit2.Response
import retrofit2.http.GET
import retrofit2.http.Header
import retrofit2.http.Path
import retrofit2.http.Query

interface PoiskKinoApi {
    @GET("v1.4/movie")
    suspend fun getMovies(
        @Header("X-API-KEY") token: String,
        @Query("limit") limit: Int = 20,
        @Query("page") page: Int = 1,
        @Query("sortField") sortField: String = "rating.kp",
        @Query("sortType") sortType: String = "-1",
    ): Response<MovieResponse>

    @GET("v1.4/movie/search")
    suspend fun searchMovies(
        @Header("X-API-KEY") token: String,
        @Query("query") query: String,
        @Query("limit") limit: Int = 20,
        @Query("page") page: Int = 1,
    ): Response<MovieResponse>

    @GET("v1.4/movie/{id}")
    suspend fun getMovieDetails(
        @Header("X-API-KEY") token: String,
        @Path("id") id: Int
    ): Response<Movie>

    @GET("v1.4/review")
    suspend fun getReviews(
        @Header("X-API-KEY") token: String,
        @Query("movieId") movieId: Int,
        @Query("limit") limit: Int = 10,
        @Query("page") page: Int = 1,
        @Query("sortField") sortField: String = "date",
        @Query("sortType") sortType: String = "-1"
    ): Response<ReviewResponse>
}