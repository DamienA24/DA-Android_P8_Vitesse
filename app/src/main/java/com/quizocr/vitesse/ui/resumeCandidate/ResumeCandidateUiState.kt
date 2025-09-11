package com.quizocr.vitesse.ui.resumeCandidate

import com.quizocr.vitesse.domain.model.Candidate

data class ResumeCandidateUiState(
    val isLoading: Boolean = false,
    val candidate: Candidate? = null,
    val errorMessage: String? = null

)