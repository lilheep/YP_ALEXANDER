package com.example.yp.fragments

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.content.ContentProviderCompat.requireContext
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.GridLayoutManager
import com.example.yp.R
import com.example.yp.adapter.MoviesAdapter
import com.example.yp.databinding.FragmentHomeBinding
import com.example.yp.network.repository.MovieRepository
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class HomeFragment : Fragment() {

    private var _binding: FragmentHomeBinding? = null
    private val binding get() = _binding!!
    private val movieRepository = MovieRepository()
    private lateinit var moviesAdapter: MoviesAdapter

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        _binding = FragmentHomeBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        setupRecyclerView()
        loadMovies()
    }

    private fun setupRecyclerView() {
        moviesAdapter = MoviesAdapter(emptyList()) { movie ->
        }

        binding.recyclerView.apply {
            layoutManager = GridLayoutManager(requireContext(), 2)
            adapter = moviesAdapter
            setHasFixedSize(true)

            setPadding(8, 8, 8, 8)
            clipToPadding = false
            clipChildren = false
        }
    }

    private fun loadMovies() {
        binding.progressBar.visibility = View.VISIBLE

        CoroutineScope(Dispatchers.IO).launch {
            try {
                val movies = movieRepository.getMovies()

                withContext(Dispatchers.Main) {
                    binding.progressBar.visibility = View.GONE

                    if (movies.isNotEmpty()) {
                        moviesAdapter = MoviesAdapter(movies) { movie ->
                        }
                        binding.recyclerView.adapter = moviesAdapter
                        binding.tvEmpty.visibility = View.GONE
                    } else {
                        binding.tvEmpty.visibility = View.VISIBLE
                        binding.tvEmpty.text = "Фильмы не найдены"
                    }
                }
            } catch (e: Exception) {
                withContext(Dispatchers.Main) {
                    binding.progressBar.visibility = View.GONE
                    binding.tvEmpty.visibility = View.VISIBLE
                    binding.tvEmpty.text = "Ошибка загрузки: ${e.message}"
                }
            }
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    companion object {
        @JvmStatic
        fun newInstance() = HomeFragment()
    }
}