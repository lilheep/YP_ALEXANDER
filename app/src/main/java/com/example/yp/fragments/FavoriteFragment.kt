package com.example.yp.fragments

import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.yp.activity.MovieDetailActivity
import com.example.yp.adapter.FavoriteMoviesAdapter
import com.example.yp.database.DBHelper
import com.example.yp.databinding.FragmentFavoriteBinding
import com.example.yp.network.models.FavoriteMovie
import com.example.yp.utils.SessionManager
import com.google.android.material.dialog.MaterialAlertDialogBuilder

class FavoriteFragment : Fragment() {

    private var _binding: FragmentFavoriteBinding? = null
    private val binding get() = _binding!!
    private lateinit var sessionManager: SessionManager
    private lateinit var dbHelper: DBHelper
    private lateinit var adapter: FavoriteMoviesAdapter
    private var favoritesList = mutableListOf<FavoriteMovie>()

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        _binding = FragmentFavoriteBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        sessionManager = SessionManager(requireContext())
        dbHelper = DBHelper(requireContext(), null)

        setupRecyclerView()
        loadFavorites()
    }

    private fun setupRecyclerView() {
        adapter = FavoriteMoviesAdapter(
            favoritesList,
            onItemClick = { favorite ->
                openMovieDetails(favorite.movieId)
            },
            onFavoriteClick = { favorite ->
                showRemoveFavoriteDialog(favorite)
            }
        )

        binding.rvFavorites.layoutManager = LinearLayoutManager(requireContext())
        binding.rvFavorites.adapter = adapter
    }

    private fun loadFavorites() {
        binding.progressBar.visibility = View.VISIBLE
        binding.tvEmpty.visibility = View.GONE
        binding.tvNotLoggedIn.visibility = View.GONE
        binding.rvFavorites.visibility = View.GONE

        if (!sessionManager.isLoggedIn()) {
            binding.progressBar.visibility = View.GONE
            binding.tvNotLoggedIn.visibility = View.VISIBLE
            return
        }

        val userId = sessionManager.getUserId()
        if (userId != null) {
            val favorites = dbHelper.getFavorites(userId)

            binding.progressBar.visibility = View.GONE

            if (favorites.isEmpty()) {
                binding.tvEmpty.visibility = View.VISIBLE
            } else {
                binding.rvFavorites.visibility = View.VISIBLE
                favoritesList.clear()
                favoritesList.addAll(favorites)
                adapter.notifyDataSetChanged()
            }
        } else {
            binding.progressBar.visibility = View.GONE
            binding.tvNotLoggedIn.visibility = View.VISIBLE
        }
    }

    private fun showRemoveFavoriteDialog(favorite: FavoriteMovie) {
        val userId = sessionManager.getUserId()
        if (userId == null) return

        MaterialAlertDialogBuilder(requireContext())
            .setTitle("Удалить из избранного")
            .setMessage("Вы уверены, что хотите удалить \"${favorite.movieTitle}\" из избранного?")
            .setPositiveButton("Удалить") { dialog, _ ->
                val success = dbHelper.removeFavorite(userId, favorite.movieId)
                if (success) {
                    val position = favoritesList.indexOfFirst { it.movieId == favorite.movieId }
                    if (position != -1) {
                        favoritesList.removeAt(position)
                        adapter.notifyItemRemoved(position)

                        if (favoritesList.isEmpty()) {
                            binding.rvFavorites.visibility = View.GONE
                            binding.tvEmpty.visibility = View.VISIBLE
                        }

                        Toast.makeText(
                            requireContext(),
                            "Фильм удален из избранного",
                            Toast.LENGTH_SHORT
                        ).show()
                    }
                }
                dialog.dismiss()
            }
            .setNegativeButton("Отмена", null)
            .show()
    }

    private fun openMovieDetails(movieId: Int) {
        val intent = Intent(requireContext(), MovieDetailActivity::class.java).apply {
            putExtra("MOVIE_ID", movieId)
        }
        startActivity(intent)
    }

    override fun onResume() {
        super.onResume()
        loadFavorites()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    companion object {
        @JvmStatic
        fun newInstance() = FavoriteFragment()
    }
}