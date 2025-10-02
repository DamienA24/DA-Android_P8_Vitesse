package com.quizocr.vitesse.ui.addEditCandidate

import com.quizocr.vitesse.domain.model.Candidate

data class AddEditCandidateUiState(
    val candidate: Candidate? = null,
    val isLoadingCandidateData: Boolean = false,
    val errorMessage: String? = null,
    val screenTitle: String = "",
    val isSaving: Boolean = false,
    val saveSuccess: Boolean = false
)
