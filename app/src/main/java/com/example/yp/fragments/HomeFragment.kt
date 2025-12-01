package com.example.yp.fragments

import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.inputmethod.EditorInfo
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.yp.R
import com.example.yp.adapter.MoviesAdapter
import com.example.yp.databinding.FragmentHomeBinding
import com.example.yp.network.repository.MovieRepository
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class HomeFragment : Fragment() {

    private var _binding: FragmentHomeBinding? = null
    private val binding get() = _binding!!
    private val movieRepository = MovieRepository()
    private lateinit var moviesAdapter: MoviesAdapter
    private var currentPage = 1
    private var totalPages = 1
    private var isLoading = false
    private var isLastPage = false
    private var searchQuery: String? = null
    private var searchJob: Job? = null
    private val moviesList = mutableListOf<com.example.yp.network.models.Movie>()

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

        setupSearchView()
        setupRecyclerView()
        loadFirstPage()
    }

    private fun setupSearchView() {
        binding.searchEditText.setOnEditorActionListener { _, actionId, _ ->
            if (actionId == EditorInfo.IME_ACTION_SEARCH) {
                performSearch()
                return@setOnEditorActionListener true
            }
            false
        }

        binding.searchEditText.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}

            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {}

            override fun afterTextChanged(s: Editable?) {
                searchJob?.cancel()

                val query = s.toString().trim()
                if (query.isEmpty()) {
                    searchQuery = null
                    loadFirstPage()
                } else {
                    searchJob = CoroutineScope(Dispatchers.Main).launch {
                        delay(500)
                        searchQuery = query
                        loadFirstPage()
                    }
                }
            }
        })
    }

    private fun performSearch() {
        val query = binding.searchEditText.text.toString().trim()
        if (query.isNotEmpty()) {
            searchQuery = query
            loadFirstPage()
            binding.searchEditText.clearFocus()
            val imm = requireContext().getSystemService(android.content.Context.INPUT_METHOD_SERVICE) as android.view.inputmethod.InputMethodManager
            imm.hideSoftInputFromWindow(binding.searchEditText.windowToken, 0)
        }
    }

    private fun setupRecyclerView() {
        moviesAdapter = MoviesAdapter(moviesList) { movie ->
        }

        binding.recyclerView.apply {
            layoutManager = GridLayoutManager(requireContext(), 2)
            adapter = moviesAdapter
            setHasFixedSize(true)

            addOnScrollListener(object : RecyclerView.OnScrollListener() {
                override fun onScrolled(recyclerView: RecyclerView, dx: Int, dy: Int) {
                    super.onScrolled(recyclerView, dx, dy)

                    val layoutManager = recyclerView.layoutManager as GridLayoutManager
                    val visibleItemCount = layoutManager.childCount
                    val totalItemCount = layoutManager.itemCount
                    val firstVisibleItemPosition = layoutManager.findFirstVisibleItemPosition()

                    if (!isLoading && !isLastPage) {
                        if ((visibleItemCount + firstVisibleItemPosition) >= totalItemCount
                            && firstVisibleItemPosition >= 0
                            && totalItemCount >= 20) {
                            loadNextPage()
                        }
                    }
                }
            })
        }
    }

    private fun loadFirstPage() {
        currentPage = 1
        isLastPage = false
        moviesList.clear()
        moviesAdapter.notifyDataSetChanged()
        loadMovies()
    }

    private fun loadNextPage() {
        if (currentPage < totalPages) {
            currentPage++
            loadMovies()
        } else {
            isLastPage = true
        }
    }

    private fun loadMovies() {
        if (isLoading) return

        isLoading = true
        binding.progressBar.visibility = View.VISIBLE
        binding.tvEmpty.visibility = View.GONE

        CoroutineScope(Dispatchers.IO).launch {
            try {
                val response = if (searchQuery.isNullOrEmpty()) {
                    movieRepository.getMovies(currentPage)
                } else {
                    movieRepository.searchMovies(searchQuery!!, currentPage)
                }

                withContext(Dispatchers.Main) {
                    isLoading = false
                    binding.progressBar.visibility = View.GONE

                    if (response.docs.isNotEmpty()) {
                        val startPosition = moviesList.size
                        moviesList.addAll(response.docs)
                        moviesAdapter.notifyItemRangeInserted(startPosition, response.docs.size)

                        totalPages = response.pages

                        if (moviesList.isEmpty()) {
                            showEmptyMessage()
                        } else {
                            binding.tvEmpty.visibility = View.GONE
                        }
                    } else if (moviesList.isEmpty()) {
                        showEmptyMessage()
                    }
                }
            } catch (e: Exception) {
                withContext(Dispatchers.Main) {
                    isLoading = false
                    binding.progressBar.visibility = View.GONE
                    if (moviesList.isEmpty()) {
                        binding.tvEmpty.visibility = View.VISIBLE
                        binding.tvEmpty.text = "Ошибка загрузки: ${e.message}"
                    }
                }
            }
        }
    }

    private fun showEmptyMessage() {
        binding.tvEmpty.visibility = View.VISIBLE
        binding.tvEmpty.text = if (searchQuery.isNullOrEmpty()) {
            "Фильмы не найдены"
        } else {
            "По запросу \"$searchQuery\" ничего не найдено"
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        searchJob?.cancel()
        _binding = null
    }

    companion object {
        @JvmStatic
        fun newInstance() = HomeFragment()
    }
}