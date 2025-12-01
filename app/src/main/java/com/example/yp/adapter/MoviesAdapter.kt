package com.example.yp.adapter

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import coil.load
import coil.transform.RoundedCornersTransformation
import com.example.yp.R
import com.example.yp.databinding.ItemMovieBinding
import com.example.yp.network.models.Movie
import java.util.Locale

class MoviesAdapter(
    private val movies: List<Movie>,
    private val onItemClick: (Movie) -> Unit
) : RecyclerView.Adapter<MoviesAdapter.MovieViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): MovieViewHolder {
        val binding = ItemMovieBinding.inflate(
            LayoutInflater.from(parent.context),
            parent,
            false
        )
        return MovieViewHolder(binding)
    }

    override fun onBindViewHolder(holder: MovieViewHolder, position: Int) {
        holder.bind(movies[position])
    }

    override fun getItemCount() = movies.size

    inner class MovieViewHolder(
        private val binding: ItemMovieBinding
    ) : RecyclerView.ViewHolder(binding.root) {

        fun bind(movie: Movie) {
            binding.apply {
                tvTitle.text = movie.name ?: movie.alternativeName ?: "Без названия"

                tvYear.text = movie.year?.toString() ?: "Неизвестен"

                val countries = movie.countries?.take(2)?.joinToString(", ") { it.name }
                tvCountry.text = countries ?: "Неизвестно"

                val rating = movie.rating?.kp
                tvRating.text = if (rating != null && rating > 0) {
                    String.format(Locale.getDefault(), "%.1f", rating)
                } else {
                    "—"
                }

                val genres = movie.genres?.take(2)?.joinToString(", ") { it.name }
                tvGenres.text = genres ?: "Не указаны"

                val posterUrl = movie.poster?.previewUrl ?: movie.poster?.url
                if (!posterUrl.isNullOrEmpty()) {
                    ivPoster.load(posterUrl) {
                        crossfade(true)
                        transformations(RoundedCornersTransformation(12f))
                        placeholder(R.drawable.placeholder_poster)
                        error(R.drawable.placeholder_poster)
                    }
                } else {
                    ivPoster.setImageResource(R.drawable.placeholder_poster)
                }

                cardView.setOnClickListener {
                    onItemClick(movie)
                }
            }
        }
    }
}