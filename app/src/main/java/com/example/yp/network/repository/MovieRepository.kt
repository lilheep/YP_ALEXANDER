package com.example.yp.network.repository

import com.example.yp.network.api.ApiClient
import com.example.yp.network.models.Movie
import com.example.yp.utils.Constants.API_TOKEN

class MovieRepository {
    private val api = ApiClient.instance

    suspend fun getMovies(page: Int = 1): List<Movie> {
        return try {
            val response = api.getMovies(
                token = API_TOKEN,
                limit = 20,
                page = page
            )
            if (response.isSuccessful) {
                response.body()?.docs ?: emptyList()
            } else {
                emptyList()
            }
        } catch (e: Exception) {
            emptyList()
        }
    }
}