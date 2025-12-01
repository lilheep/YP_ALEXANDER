package com.example.yp.adapter

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.example.yp.databinding.ItemReviewBinding
import com.example.yp.network.models.Review
import java.text.SimpleDateFormat
import java.util.Locale

class ReviewsAdapter(
    private val reviews: MutableList<Review>
) : RecyclerView.Adapter<ReviewsAdapter.ReviewViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ReviewViewHolder {
        val binding = ItemReviewBinding.inflate(
            LayoutInflater.from(parent.context),
            parent,
            false
        )
        return ReviewViewHolder(binding)
    }

    override fun onBindViewHolder(holder: ReviewViewHolder, position: Int) {
        holder.bind(reviews[position])
    }

    override fun getItemCount() = reviews.size

    inner class ReviewViewHolder(
        private val binding: ItemReviewBinding
    ) : RecyclerView.ViewHolder(binding.root) {

        fun bind(review: Review) {
            binding.apply {
                tvReviewTitle.text = review.title ?: "Без названия"
                tvReviewAuthor.text = review.author ?: "Аноним"
                tvReviewText.text = review.review ?: "Текст рецензии отсутствует"

                review.date?.let { dateStr ->
                    try {
                        val inputFormat = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'", Locale.getDefault())
                        val outputFormat = SimpleDateFormat("dd.MM.yyyy", Locale.getDefault())
                        val date = inputFormat.parse(dateStr)
                        tvReviewDate.text = date?.let { outputFormat.format(it) } ?: dateStr
                    } catch (e: Exception) {
                        tvReviewDate.text = dateStr
                    }
                } ?: run {
                    tvReviewDate.text = ""
                }

                val userRating = review.userRating
                tvUserRating.text = if (userRating != null && userRating > 0) {
                    "$userRating/10"
                } else {
                    "—"
                }

                val type = review.type
                tvReviewType.text = when (type) {
                    "Позитивный" -> "👍 Положительная"
                    "Негативный" -> "👎 Отрицательная"
                    "Нейтральный" -> "😐 Нейтральная"
                    else -> "📝 Рецензия"
                }
            }
        }
    }
}