package com.example.yp.network.repository

import com.example.yp.network.api.ApiClient
import com.example.yp.network.models.MovieResponse
import com.example.yp.utils.Constants.API_TOKEN

class MovieRepository {
    private val api = ApiClient.instance

    suspend fun getMovies(page: Int = 1): MovieResponse {
        return try {
            val response = api.getMovies(
                token = API_TOKEN,
                limit = 20,
                page = page,
                sortField = "rating.kp",
                sortType = "-1"
            )

            println("API Response Code: ${response.code()}")
            println("API Response Message: ${response.message()}")

            if (response.isSuccessful) {
                val movieResponse = response.body() ?: MovieResponse(emptyList(), 0, 0, page, 0)
                println("Movies received: ${movieResponse.docs.size}")
                println("Total pages: ${movieResponse.pages}")
                movieResponse
            } else {
                val errorBody = response.errorBody()?.string()
                println("API Error Body: $errorBody")
                MovieResponse(emptyList(), 0, 0, page, 0)
            }
        } catch (e: Exception) {
            println("Exception in MovieRepository: ${e.message}")
            e.printStackTrace()
            MovieResponse(emptyList(), 0, 0, page, 0)
        }

    }
    suspend fun searchMovies(query: String, page: Int = 1): MovieResponse {
        return api.searchMovies(
            token = API_TOKEN,
            query = query,
            page = page
        ).body() ?: throw Exception("Empty response")
    }
}