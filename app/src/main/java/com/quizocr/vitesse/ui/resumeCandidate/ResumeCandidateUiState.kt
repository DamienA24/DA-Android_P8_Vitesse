package com.quizocr.vitesse.ui.resumeCandidate

import com.quizocr.vitesse.domain.model.Candidate

data class ResumeCandidateUiState(
    val isLoading: Boolean = false,
    val candidate: Candidate? = null,
    val errorMessage: String? = null,

    val isConvertingCurrency: Boolean = false,
    val formattedSalaryPounds: String? = null,
    val conversionErrorMessage: String? = null,

    val isDeleting: Boolean = false,
    val deletionErrorMessage: String? = null,
    val navigateBackAfterDeletion: Boolean = false

)