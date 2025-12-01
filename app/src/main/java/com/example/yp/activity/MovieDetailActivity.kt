package com.example.yp.activity

import android.os.Bundle
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.PagerSnapHelper
import androidx.recyclerview.widget.RecyclerView
import coil.load
import coil.transform.RoundedCornersTransformation
import com.example.yp.R
import com.example.yp.adapter.ReviewsAdapter
import com.example.yp.databinding.ActivityMovieDetailBinding
import com.example.yp.network.repository.MovieRepository
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.text.NumberFormat
import java.util.Locale

class MovieDetailActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMovieDetailBinding
    private val movieRepository = MovieRepository()
    private lateinit var reviewsAdapter: ReviewsAdapter
    private var movieId: Int = 0
    private var currentReviewPage = 1
    private var totalReviewPages = 1
    private val reviewsList = mutableListOf<com.example.yp.network.models.Review>()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMovieDetailBinding.inflate(layoutInflater)
        setContentView(binding.root)

        movieId = intent.getIntExtra("MOVIE_ID", 0)
        if (movieId == 0) {
            finish()
            return
        }

        setupViews()
        loadMovieDetails()
        setupReviews()
    }

    private fun setupViews() {
        binding.toolbar.setNavigationOnClickListener {
            onBackPressedDispatcher.onBackPressed()
        }

        binding.ivBackdrop.clipToOutline = true
    }

    private fun setupReviews() {
        reviewsAdapter = ReviewsAdapter(reviewsList)
        binding.rvReviews.apply {
            layoutManager = LinearLayoutManager(this@MovieDetailActivity, LinearLayoutManager.HORIZONTAL, false)
            adapter = reviewsAdapter
            setHasFixedSize(true)

            val snapHelper = PagerSnapHelper()
            snapHelper.attachToRecyclerView(this)

            addOnScrollListener(object : RecyclerView.OnScrollListener() {
                override fun onScrolled(recyclerView: RecyclerView, dx: Int, dy: Int) {
                    super.onScrolled(recyclerView, dx, dy)
                    val layoutManager = recyclerView.layoutManager as LinearLayoutManager
                    val firstVisiblePosition = layoutManager.findFirstVisibleItemPosition()
                    binding.tvReviewIndicator.text = "${firstVisiblePosition + 1}/${reviewsList.size}"
                }
            })
        }
    }

    private fun loadMovieDetails() {
        binding.progressBar.visibility = View.VISIBLE
        binding.scrollView.visibility = View.GONE

        CoroutineScope(Dispatchers.IO).launch {
            try {
                val movie = movieRepository.getMovieDetails(movieId)

                withContext(Dispatchers.Main) {
                    binding.progressBar.visibility = View.GONE
                    binding.scrollView.visibility = View.VISIBLE

                    movie.backdrop?.url?.let { backdropUrl ->
                        binding.ivBackdrop.load(backdropUrl) {
                            crossfade(true)
                            placeholder(R.drawable.placeholder_backdrop)
                            error(R.drawable.placeholder_backdrop)
                        }
                    }

                    movie.poster?.url?.let { posterUrl ->
                        binding.ivPoster.load(posterUrl) {
                            crossfade(true)
                            transformations(RoundedCornersTransformation(12f))
                            placeholder(R.drawable.placeholder_poster)
                            error(R.drawable.placeholder_poster)
                        }
                    }

                    binding.tvTitle.text = movie.name ?: movie.alternativeName ?: "Без названия"
                    binding.tvOriginalTitle.text = movie.alternativeName ?: ""

                    val rating = movie.rating?.kp
                    binding.tvRating.text = if (rating != null && rating > 0) {
                        String.format(Locale.getDefault(), "%.1f", rating)
                    } else {
                        "—"
                    }

                    val year = movie.year?.toString() ?: "Неизвестен"
                    val countries = movie.countries?.joinToString(", ") { it.name ?: "" } ?: "Неизвестно"
                    val genres = movie.genres?.joinToString(", ") { it.name ?: "" } ?: "Не указаны"
                    binding.tvMeta.text = "$year • $countries • $genres"

                    binding.tvDescription.text = movie.description ?: movie.shortDescription ?: "Описание отсутствует"

                    val duration = movie.movieLength?.let { "$it мин" } ?: "Неизвестно"
                    binding.tvDuration.text = duration

                    val ageRating = movie.ageRating?.let { "$it+" } ?: "Не указан"
                    binding.tvAgeRating.text = ageRating

                    val budget = movie.budget?.value?.let {
                        NumberFormat.getNumberInstance(Locale.US).format(it) + " " + (movie.budget.currency ?: "")
                    } ?: "Неизвестен"
                    binding.tvBudget.text = budget

                    val directors = movie.persons
                        ?.filter { it.profession?.contains("режиссер") == true || it.profession?.contains("director") == true }
                        ?.take(3)
                        ?.joinToString(", ") { it.name ?: "" }
                    binding.tvDirector.text = directors ?: "Не указаны"

                    val actors = movie.persons
                        ?.filter { it.profession?.contains("актер") == true || it.profession?.contains("actor") == true }
                        ?.take(5)
                        ?.joinToString(", ") { it.name ?: "" }
                    binding.tvActors.text = actors ?: "Не указаны"

                    val premiere = movie.premiere?.russia ?: movie.premiere?.world ?: "Неизвестна"
                    binding.tvPremiere.text = premiere

                    val boxOffice = movie.fees?.world?.value?.let {
                        NumberFormat.getNumberInstance(Locale.US).format(it) + " " + (movie.fees.world.currency ?: "")
                    } ?: "Неизвестны"
                    binding.tvBoxOffice.text = boxOffice

                    loadReviews()
                }
            } catch (e: Exception) {
                withContext(Dispatchers.Main) {
                    binding.progressBar.visibility = View.GONE
                    binding.tvError.visibility = View.VISIBLE
                    binding.tvError.text = "Ошибка загрузки: ${e.message}"
                }
            }
        }
    }

    private fun loadReviews() {
        CoroutineScope(Dispatchers.IO).launch {
            try {
                val response = movieRepository.getReviews(movieId, currentReviewPage)

                withContext(Dispatchers.Main) {
                    if (response.docs.isNotEmpty()) {
                        reviewsList.addAll(response.docs)
                        reviewsAdapter.notifyDataSetChanged()
                        totalReviewPages = response.pages

                        binding.tvReviewIndicator.text = "1/${reviewsList.size}"
                        binding.reviewsContainer.visibility = View.VISIBLE
                    } else {
                        binding.tvNoReviews.visibility = View.VISIBLE
                    }
                }
            } catch (e: Exception) {
                withContext(Dispatchers.Main) {
                    binding.tvNoReviews.visibility = View.VISIBLE
                    binding.tvNoReviews.text = "Не удалось загрузить рецензии"
                }
            }
        }
    }
}