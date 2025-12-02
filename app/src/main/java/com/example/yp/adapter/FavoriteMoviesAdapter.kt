package com.example.yp.adapter

import android.view.View
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import coil.load
import coil.transform.RoundedCornersTransformation
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

                val posterUrl = favorite.moviePosterUrl
                if (posterUrl.isNotEmpty()) {
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
                    onItemClick(favorite)
                }

                cardView.setOnLongClickListener {
                    onFavoriteClick(favorite)
                    true
                }
            }
        }
    }
}