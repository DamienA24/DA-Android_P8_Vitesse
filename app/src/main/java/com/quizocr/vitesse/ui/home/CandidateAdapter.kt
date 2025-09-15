package com.quizocr.vitesse.ui.home

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.quizocr.vitesse.R
import com.quizocr.vitesse.data.entity.CandidateEntity
import com.quizocr.vitesse.databinding.ItemCandidateBinding
import com.quizocr.vitesse.domain.model.CandidateSummary

class CandidateAdapter(
    private var candidates: List<CandidateSummary>,
    private val onCandidateClick: (CandidateSummary) -> Unit,
    private val onFavoriteClick: (CandidateEntity) -> Unit
) : RecyclerView.Adapter<CandidateAdapter.CandidateViewHolder>() {

    fun updateData(newCandidates: List<CandidateSummary>) {
        this.candidates = newCandidates
        notifyDataSetChanged()
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): CandidateViewHolder {

        val itemBinding = ItemCandidateBinding.inflate(
            LayoutInflater.from(parent.context),
            parent,
            false
        )
        return CandidateViewHolder(itemBinding)
    }

    override fun onBindViewHolder(holder: CandidateViewHolder, position: Int) {
        holder.bind(candidates[position])
        holder.itemView.setOnClickListener {
            onCandidateClick(candidates[position])
        }
    }

    override fun getItemCount(): Int {
        return candidates.size
    }
    inner class CandidateViewHolder(private val binding: ItemCandidateBinding) : RecyclerView.ViewHolder(binding.root) {
        private val photoImageView = binding.ivAvatar
        private val nameTextView: TextView = binding.tvCandidateName
        private val descriptionTextView: TextView = binding.tvCandidateDescription

        fun bind(candidate: CandidateSummary) {
            nameTextView.text = "${candidate.firstName} ${candidate.lastName}"
            descriptionTextView.text = candidate.notes

            if (!candidate.photoUri.isNullOrBlank()) {
                Glide.with(itemView.context)
                    .load(candidate.photoUri)
                    .placeholder(R.drawable.ic_android_black_24dp)
                    .error(R.drawable.ic_android_black_24dp)
                    .into(photoImageView)
            } else {
                photoImageView.setImageResource(R.drawable.ic_android_black_24dp)
            }
        }
    }
}