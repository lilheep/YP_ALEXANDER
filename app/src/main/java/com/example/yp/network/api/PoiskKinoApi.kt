package com.example.yp.network.api

import com.example.yp.network.models.MovieResponse
import retrofit2.Response
import retrofit2.http.GET
import retrofit2.http.Header
import retrofit2.http.Query

interface PoiskKinoApi {
    @GET("v1.4/movie")
    suspend fun getMovies(
        @Header("X-API-KEY") token: String,
        @Query("limit") limit: Int = 20,
        @Query("page") page: Int = 1,
        @Query("selectFields") selectFields: String = "id name alternativeName year description rating poster countries genres",
        @Query("sortField") sortField: String = "rating.kp",
        @Query("sortType") sortType: String = "-1"
    ): Response<MovieResponse>
}