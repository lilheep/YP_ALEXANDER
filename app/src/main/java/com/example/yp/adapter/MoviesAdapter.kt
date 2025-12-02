package com.example.yp.adapter

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import coil.load
import coil.transform.RoundedCornersTransformation
import coil.request.CachePolicy
import com.example.yp.R
import com.example.yp.databinding.ItemMovieBinding
import com.example.yp.network.models.Movie
import java.util.Locale

class MoviesAdapter(
    private val movies: MutableList<Movie>,
    private val onItemClick: (Movie) -> Unit
) : RecyclerView.Adapter<MoviesAdapter.MovieViewHolder>() {

    // Флаг для отслеживания, отменять ли предыдущие загрузки
    private var shouldClearImageOnBind = true

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): MovieViewHolder {
        val binding = ItemMovieBinding.inflate(
            LayoutInflater.from(parent.context),
            parent,
            false
        )
        return MovieViewHolder(binding)
    }

    override fun onBindViewHolder(holder: MovieViewHolder, position: Int) {
        holder.bind(movies[position], position)
    }

    override fun getItemCount() = movies.size

    override fun onViewRecycled(holder: MovieViewHolder) {
        super.onViewRecycled(holder)
        if (shouldClearImageOnBind) {
            holder.clearImage()
        }
    }

    fun updateData(newMovies: List<Movie>) {
        movies.clear()
        movies.addAll(newMovies)
        notifyDataSetChanged()
    }

    fun setShouldClearImageOnBind(shouldClear: Boolean) {
        shouldClearImageOnBind = shouldClear
    }

    inner class MovieViewHolder(
        private val binding: ItemMovieBinding
    ) : RecyclerView.ViewHolder(binding.root) {

        fun bind(movie: Movie, position: Int) {
            binding.apply {
                if (shouldClearImageOnBind) {
                    ivPoster.setImageDrawable(null)
                }

                tvTitle.text = movie.name ?: movie.alternativeName ?: "Без названия"
                tvYear.text = movie.year?.toString() ?: "Неизвестен"

                val countries = movie.countries?.take(2)?.joinToString(", ") { it.name.toString() }
                tvCountry.text = countries ?: "Неизвестно"

                val rating = movie.rating?.kp
                tvRating.text = if (rating != null && rating > 0) {
                    String.format(Locale.getDefault(), "%.1f", rating)
                } else {
                    "—"
                }

                val genres = movie.genres?.take(2)?.joinToString(", ") { it.name.toString() }
                tvGenres.text = genres ?: "Не указаны"

                loadMoviePoster(movie, position)

                cardView.setOnClickListener {
                    onItemClick(movie)
                }
            }
        }

        private fun loadMoviePoster(movie: Movie, position: Int) {
            val posterUrl = getValidPosterUrl(movie)

            if (posterUrl != null) {
                binding.ivPoster.load(posterUrl) {
                    crossfade(true)
                    transformations(RoundedCornersTransformation(12f))
                    placeholder(R.drawable.placeholder_poster)
                    error(R.drawable.placeholder_poster)

                    memoryCachePolicy(CachePolicy.ENABLED)
                    diskCachePolicy(CachePolicy.ENABLED)

                    size(400, 600)

                    allowHardware(true)

                    listener(
                        onStart = {
                        },
                        onSuccess = { _, _ ->
                        },
                        onError = { _, throwable ->
                            binding.ivPoster.setImageResource(R.drawable.glow_red)
                        }
                    )
                }
            } else {
                binding.ivPoster.load(R.drawable.glow_red) {
                    crossfade(true)
                    transformations(RoundedCornersTransformation(12f))
                    memoryCachePolicy(CachePolicy.ENABLED)
                    diskCachePolicy(CachePolicy.ENABLED)
                }
            }
        }

        private fun getValidPosterUrl(movie: Movie): String? {
            val posterUrl = movie.poster?.previewUrl ?: movie.poster?.url

            if (posterUrl.isNullOrEmpty()) return null
            if (posterUrl.equals("null", ignoreCase = true)) return null
            if (!posterUrl.startsWith("http://") && !posterUrl.startsWith("https://")) return null

            return try {
                java.net.URL(posterUrl)
                posterUrl
            } catch (e: Exception) {
                null
            }
        }

        fun clearImage() {
            binding.ivPoster.setImageDrawable(null)
        }
    }
}