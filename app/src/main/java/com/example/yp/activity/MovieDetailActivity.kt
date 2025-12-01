package com.example.yp.activity

import android.os.Bundle
import android.util.Log
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.PagerSnapHelper
import androidx.recyclerview.widget.RecyclerView
import coil.load
import com.example.yp.R
import com.example.yp.adapter.ReviewsAdapter
import com.example.yp.database.DBHelper
import com.example.yp.databinding.ActivityMovieDetailBinding
import com.example.yp.network.repository.MovieRepository
import com.example.yp.utils.SessionManager
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.text.NumberFormat
import java.util.Locale

class MovieDetailActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMovieDetailBinding
    private val movieRepository = MovieRepository()
    private lateinit var dbHelper: DBHelper
    private lateinit var sessionManager: SessionManager
    private lateinit var reviewsAdapter: ReviewsAdapter
    private var movieId: Int = 0
    private var movieTitle: String = ""
    private var moviePosterUrl: String? = null
    private var movieYear: Int? = null
    private var movieRating: Double? = null
    private var currentReviewPage = 1
    private var totalReviewPages = 1
    private val reviewsList = mutableListOf<com.example.yp.network.models.Review>()
    private var isFavorite: Boolean = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMovieDetailBinding.inflate(layoutInflater)
        setContentView(binding.root)

        movieId = intent.getIntExtra("MOVIE_ID", 0)
        if (movieId == 0) {
            finish()
            return
        }

        dbHelper = DBHelper(this, null)
        sessionManager = SessionManager(this)

        setupViews()
        loadMovieDetails()
        setupReviews()
    }

    private fun setupViews() {
        binding.toolbar.setNavigationOnClickListener {
            onBackPressedDispatcher.onBackPressed()
        }

        binding.ivBackdrop.clipToOutline = true

        binding.btnFavorite.setOnClickListener {
            toggleFavorite()
        }
    }

    private fun checkFavoriteStatus() {
        val userId = sessionManager.getUserId()
        Log.d("MovieDetail", "User ID: $userId, Movie ID: $movieId")

        if (userId != null) {
            isFavorite = dbHelper.isFavorite(userId, movieId)
            Log.d("MovieDetail", "Is favorite: $isFavorite")
            updateFavoriteButton()
        } else {
            Log.d("MovieDetail", "User not logged in")
            binding.btnFavorite.isEnabled = true
            binding.btnFavorite.alpha = 1f
            binding.btnFavorite.setImageResource(R.drawable.baseline_favorite_border_24)
        }
    }

    private fun animateFavoriteButton() {
        binding.btnFavorite.animate()
            .scaleX(1.2f)
            .scaleY(1.2f)
            .setDuration(150)
            .withEndAction {
                binding.btnFavorite.animate()
                    .scaleX(1f)
                    .scaleY(1f)
                    .setDuration(150)
                    .start()
            }
            .start()
    }

    private fun toggleFavorite() {
        val userId = sessionManager.getUserId()
        if (userId == null) {
            showLoginRequiredDialog()
            return
        }

        animateFavoriteButton()

        if (isFavorite) {
            val removed = dbHelper.removeFavorite(userId, movieId)
            if (removed) {
                isFavorite = false
                updateFavoriteButton()
                showFavoriteRemovedSnackbar()
            }
        } else {
            val added = dbHelper.addFavorite(
                userId = userId,
                movieId = movieId,
                movieTitle = movieTitle,
                moviePosterUrl = moviePosterUrl,
                movieYear = movieYear,
                movieRating = movieRating
            )
            if (added > 0) {
                isFavorite = true
                updateFavoriteButton()
                showFavoriteAddedSnackbar()
            }
        }
    }

    private fun updateFavoriteButton() {
        if (isFavorite) {
            binding.btnFavorite.setImageResource(R.drawable.ic_favorite_filled)
        } else {
            binding.btnFavorite.setImageResource(R.drawable.baseline_favorite_border_24)
        }
    }

    private fun showLoginRequiredDialog() {
        androidx.appcompat.app.AlertDialog.Builder(this)
            .setTitle("Требуется вход")
            .setMessage("Для добавления в избранное необходимо войти в аккаунт")
            .setPositiveButton("Войти") { dialog, _ ->
                dialog.dismiss()
            }
            .setNegativeButton("Отмена", null)
            .show()
    }

    private fun showFavoriteAddedSnackbar() {
        com.google.android.material.snackbar.Snackbar.make(
            binding.root,
            "Фильм добавлен в избранное",
            com.google.android.material.snackbar.Snackbar.LENGTH_SHORT
        ).show()
    }

    private fun showFavoriteRemovedSnackbar() {
        com.google.android.material.snackbar.Snackbar.make(
            binding.root,
            "Фильм удален из избранного",
            com.google.android.material.snackbar.Snackbar.LENGTH_SHORT
        ).show()
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

                    // Сохраняем данные для избранного
                    movieTitle = movie.name ?: movie.alternativeName ?: "Без названия"
                    moviePosterUrl = movie.poster?.url
                    movieYear = movie.year
                    movieRating = movie.rating?.kp

                    // Загрузка backdrop или poster как fallback
                    val backdropUrl = movie.backdrop?.url
                    val posterUrl = movie.poster?.url

                    when {
                        !backdropUrl.isNullOrEmpty() -> {
                            binding.ivBackdrop.load(backdropUrl) {
                                crossfade(true)
                                placeholder(R.drawable.placeholder_backdrop)
                                error(R.drawable.placeholder_backdrop)
                            }
                        }
                        !posterUrl.isNullOrEmpty() -> {
                            binding.ivBackdrop.load(posterUrl) {
                                crossfade(true)
                                placeholder(R.drawable.placeholder_backdrop)
                                error(R.drawable.placeholder_backdrop)
                            }
                        }
                        else -> {
                            binding.ivBackdrop.setImageResource(R.drawable.placeholder_backdrop)
                        }
                    }

                    binding.tvTitle.text = movieTitle
                    binding.tvOriginalTitle.text = movie.alternativeName ?: ""

                    // Рейтинг
                    val rating = movie.rating?.kp
                    binding.tvRating.text = if (rating != null && rating > 0) {
                        String.format(Locale.getDefault(), "%.1f", rating)
                    } else {
                        "—"
                    }

                    // Год, страна, жанры
                    val year = movie.year?.toString() ?: "Неизвестен"
                    val countries = movie.countries?.joinToString(", ") { it.name ?: "" } ?: "Неизвестно"
                    val genres = movie.genres?.joinToString(", ") { it.name ?: "" } ?: "Не указаны"
                    binding.tvMeta.text = "$year • $countries • $genres"

                    // Описание
                    binding.tvDescription.text = movie.description ?: movie.shortDescription ?: "Описание отсутствует"

                    // Длительность
                    val duration = movie.movieLength?.let { "$it мин" } ?: "Неизвестно"
                    binding.tvDuration.text = duration

                    // Возрастной рейтинг
                    val ageRating = movie.ageRating?.let { "$it+" } ?: "Не указан"
                    binding.tvAgeRating.text = ageRating

                    // Бюджет
                    val budget = movie.budget?.value?.let {
                        NumberFormat.getNumberInstance(Locale.US).format(it) + " " + (movie.budget.currency ?: "")
                    } ?: "Неизвестен"
                    binding.tvBudget.text = budget

                    // Режиссеры
                    val directors = movie.persons
                        ?.filter { it.profession?.contains("режиссер") == true || it.profession?.contains("director") == true }
                        ?.take(3)
                        ?.joinToString(", ") { it.name ?: "" }
                    binding.tvDirector.text = directors ?: "Не указаны"

                    // Актёры
                    val actors = movie.persons
                        ?.filter { it.profession?.contains("актер") == true || it.profession?.contains("actor") == true }
                        ?.take(5)
                        ?.joinToString(", ") { it.name ?: "" }
                    binding.tvActors.text = actors ?: "Не указаны"

                    // Премьера (только дата, без времени)
                    val premiere = movie.premiere?.russia ?: movie.premiere?.world ?: "Неизвестна"
                    val premiereDate = if (premiere != "Неизвестна" && premiere.contains("T")) {
                        premiere.split("T")[0]
                    } else {
                        premiere
                    }
                    binding.tvPremiere.text = premiereDate

                    // Сборы
                    val boxOffice = movie.fees?.world?.value?.let {
                        NumberFormat.getNumberInstance(Locale.US).format(it) + " " + (movie.fees.world.currency ?: "")
                    } ?: "Неизвестны"
                    binding.tvBoxOffice.text = boxOffice

                    // Загружаем рецензии
                    loadReviews()

                    // Проверяем статус избранного ПОСЛЕ загрузки данных фильма
                    checkFavoriteStatus()
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