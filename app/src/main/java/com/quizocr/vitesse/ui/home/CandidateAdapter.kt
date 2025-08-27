package com.quizocr.vitesse.ui.home

import android.annotation.SuppressLint
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.quizocr.vitesse.R
import com.quizocr.vitesse.data.entity.CandidateEntity
import com.quizocr.vitesse.domain.model.Candidate

class CandidateAdapter(
    private var candidates: List<Candidate>,
    private val onCandidateClick: (CandidateEntity) -> Unit,
    private val onFavoriteClick: (CandidateEntity) -> Unit
) : RecyclerView.Adapter<CandidateAdapter.CandidateViewHolder>() {

    fun updateData(newCandidates: List<Candidate>) {
        this.candidates = newCandidates
        notifyDataSetChanged()
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): CandidateViewHolder {

        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_candidate, parent, false)
        return CandidateViewHolder(view)
    }

    override fun onBindViewHolder(holder: CandidateViewHolder, position: Int) {
        holder.bind(candidates[position])
    }

    override fun getItemCount(): Int {
        return candidates.size
    }
    inner class CandidateViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        private val photoImageView: ImageView = itemView.findViewById(R.id.ivAvatar)
        private val nameTextView: TextView = itemView.findViewById(R.id.tvCandidateName)
        private val descriptionTextView: TextView = itemView.findViewById(R.id.tvCandidateDescription)

        fun bind(candidate: Candidate) {
            nameTextView.text = "${candidate.firstName} ${candidate.lastName}"
            descriptionTextView.text = candidate.notes

            if (!candidate.photoUri.isNullOrBlank()) {
                // Glide.with(itemView.context)

                photoImageView.setImageResource(R.drawable.ic_android_black_24dp)
            } else {
                photoImageView.setImageResource(R.drawable.ic_android_black_24dp)
            }

            // Click listeners
            itemView.setOnClickListener {

            }
        }
    }
}