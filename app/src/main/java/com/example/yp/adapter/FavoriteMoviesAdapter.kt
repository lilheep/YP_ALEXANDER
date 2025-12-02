package com.example.yp.adapter

import android.view.View
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import coil.load
import coil.transform.RoundedCornersTransformation
import coil.request.CachePolicy
import com.example.yp.R
import com.example.yp.databinding.ItemMovieBinding
import com.example.yp.network.models.FavoriteMovie
import java.util.Locale

class FavoriteMoviesAdapter(
    private val favorites: MutableList<FavoriteMovie>,
    private val onItemClick: (FavoriteMovie) -> Unit,
    private val onFavoriteClick: (FavoriteMovie) -> Unit
) : RecyclerView.Adapter<FavoriteMoviesAdapter.FavoriteMovieViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): FavoriteMovieViewHolder {
        val binding = ItemMovieBinding.inflate(
            LayoutInflater.from(parent.context),
            parent,
            false
        )
        return FavoriteMovieViewHolder(binding)
    }

    override fun onBindViewHolder(holder: FavoriteMovieViewHolder, position: Int) {
        holder.bind(favorites[position])
    }

    override fun getItemCount() = favorites.size

    fun updateData(newFavorites: List<FavoriteMovie>) {
        favorites.clear()
        favorites.addAll(newFavorites)
        notifyDataSetChanged()
    }

    fun removeItem(position: Int) {
        if (position >= 0 && position < favorites.size) {
            favorites.removeAt(position)
            notifyItemRemoved(position)
        }
    }

    inner class FavoriteMovieViewHolder(
        private val binding: ItemMovieBinding
    ) : RecyclerView.ViewHolder(binding.root) {

        fun bind(favorite: FavoriteMovie) {
            binding.apply {
                ivPoster.setImageDrawable(null)

                tvTitle.text = favorite.movieTitle
                tvYear.text = favorite.movieYear.toString()

                val rating = favorite.movieRating
                tvRating.text = if (rating > 0) {
                    String.format(Locale.getDefault(), "%.1f", rating)
                } else {
                    "—"
                }

                tvCountry.visibility = View.GONE
                tvGenres.visibility = View.GONE

                loadPosterForFavorite(favorite)

                cardView.setOnClickListener {
                    onItemClick(favorite)
                }

                cardView.setOnLongClickListener {
                    onFavoriteClick(favorite)
                    true
                }
            }
        }

        private fun loadPosterForFavorite(favorite: FavoriteMovie) {
            val posterUrl = favorite.moviePosterUrl

            if (isValidPosterUrl(posterUrl)) {
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
                        onError = { _, _ ->
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

        private fun isValidPosterUrl(url: String): Boolean {
            if (url.isEmpty()) return false
            if (url.equals("null", ignoreCase = true)) return false
            if (!url.startsWith("http://") && !url.startsWith("https://")) return false

            return try {
                java.net.URL(url)
                true
            } catch (e: Exception) {
                false
            }
        }
    }
}