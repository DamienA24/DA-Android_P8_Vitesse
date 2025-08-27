package com.quizocr.vitesse.ui.home

import com.quizocr.vitesse.domain.model.Candidate

data class CandidateUiState(
    val isLoading: Boolean = false,
    val candidates: List<Candidate> = emptyList(),
    val errorMessage: String? = null
)
