package com.quizocr.vitesse.ui.home

import com.quizocr.vitesse.domain.model.CandidateSummary

data class CandidateUiState(
    val isLoading: Boolean = false,
    val candidates: List<CandidateSummary> = emptyList(),
    val errorMessage: String? = null,
    val searchQuery: String = ""
)
