package com.quizocr.vitesse.ui.resumeCandidate

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.quizocr.vitesse.data.repository.DataResult
import com.quizocr.vitesse.domain.usecase.DeleteCandidateUseCase
import com.quizocr.vitesse.domain.usecase.GetCandidateById
import com.quizocr.vitesse.domain.usecase.GetCurrencyConversionRateUseCase
import com.quizocr.vitesse.domain.usecase.UpdateCandidateFavoriteStatusUseCase
import com.quizocr.vitesse.utils.formatCurrencyInUk
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.flow.onStart
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class ResumeCandidateViewModel @Inject constructor(
    private val getCandidateById: GetCandidateById,
    private val getCurrencyConversionRateUseCase: GetCurrencyConversionRateUseCase,
    private val updateCandidateFavoriteStatusUseCase: UpdateCandidateFavoriteStatusUseCase,
    private val deleteCandidateUseCase: DeleteCandidateUseCase,
    savedStateHandle: SavedStateHandle) : ViewModel() {

    private val _uiState = MutableStateFlow(ResumeCandidateUiState())
    val uiState: StateFlow<ResumeCandidateUiState> = _uiState.asStateFlow()

    private val candidateId: Int = savedStateHandle.get<Int>("candidateId") ?: -1

    init {
        if (candidateId != -1 && candidateId != 0) {
            loadCandidateDetails(candidateId)

            viewModelScope.launch {
                val initialState = uiState.first {
                    it.candidate != null && !it.isLoading
                }
                initialState.candidate?.salaryEuros?.let { salaryInEuros ->
                    if (salaryInEuros > 0) {
                        convertSalaryToPounds(salaryInEuros)
                    } else {
                        _uiState.update { it.copy(isConvertingCurrency = false, formattedSalaryPounds = "N/A") }
                    }
                }
            }
        } else {
            _uiState.update {
                it.copy(
                    isLoading = false,
                    errorMessage = "Invalid Candidate ID received."
                )
            }
        }
    }

    private fun loadCandidateDetails(idToLoad: Int) {
        getCandidateById.execute(idToLoad)
            .onStart {
                _uiState.update { it.copy(isLoading = true, errorMessage = null, candidate = null, isConvertingCurrency = true) }
            }
            .onEach { result ->
                _uiState.update { currentState ->
                    when (result) {
                        is DataResult.Success -> {
                            val candidate = result.data
                            currentState.copy(
                                isLoading = false,
                                candidate = candidate,
                                errorMessage = null
                            )
                        }
                        is DataResult.Error -> {
                            currentState.copy(
                                isLoading = false,
                                candidate = null,
                                errorMessage = result.exception.message ?: "Failed to load candidate details."
                            )
                        }
                    }
                }
            }
            .launchIn(viewModelScope)
    }

    private fun convertSalaryToPounds(salaryInEuros: Double) {
        getCurrencyConversionRateUseCase("eur", "gbp")
            .onStart {
                _uiState.update {
                    it.copy(
                        isConvertingCurrency = true,
                        conversionErrorMessage = null,
                        formattedSalaryPounds = null
                    )
                }
            }
            .onEach { result ->
                _uiState.update { currentState ->
                    when (result) {
                        is DataResult.Success -> {
                            val rate = result.data.rate
                            val salaryInPounds = salaryInEuros * rate
                            currentState.copy(
                                isLoading = false,
                                isConvertingCurrency = false,
                                formattedSalaryPounds = formatCurrencyInUk(salaryInPounds),
                                conversionErrorMessage = null
                            )
                        }
                        is DataResult.Error -> {
                            currentState.copy(
                                isLoading = false,
                                isConvertingCurrency = false,
                                formattedSalaryPounds = null,
                                conversionErrorMessage = result.exception.message ?: "Currency conversion failed."
                            )
                        }
                    }
                }
            }
            .launchIn(viewModelScope)
    }

    fun toggleFavoriteStatus() {
        val currentCandidate = _uiState.value.candidate ?: return
        val newFavoriteStatus = !currentCandidate.isFavorite

        viewModelScope.launch {
            when (val result = updateCandidateFavoriteStatusUseCase.execute(
                currentCandidate.id,
                newFavoriteStatus
            )) {
                is DataResult.Success -> {
                    _uiState.update { currentState ->
                        currentState.candidate?.let { cand ->
                            currentState.copy(candidate = cand.copy(isFavorite = newFavoriteStatus))
                        } ?: currentState
                    }
                }

                is DataResult.Error -> {
                    _uiState.update {
                        it.copy(
                            errorMessage = result.exception.message
                                ?: "Failed to update favorite status."
                        )
                    }
                }
            }
        }
    }

    fun deleteCandidate() {
        val currentCandidate = _uiState.value.candidate ?: return

        viewModelScope.launch {
            _uiState.update { it.copy(isDeleting = true, deletionErrorMessage = null) }
            when (val result = deleteCandidateUseCase.execute(currentCandidate)) {
                is DataResult.Success -> {
                    _uiState.update {
                        it.copy(
                            isDeleting = false,
                            candidate = null,
                            navigateBackAfterDeletion = true
                        )
                    }
                }
                is DataResult.Error -> {
                    _uiState.update {
                        it.copy(
                            isDeleting = false,
                            deletionErrorMessage = result.exception.message ?: "Failed to delete candidate."
                        )
                    }
                }
            }
        }
    }

    fun onNavigationDone() {
        _uiState.update { it.copy(navigateBackAfterDeletion = false) }
    }

    fun clearDeletionError() {
        _uiState.update { it.copy(deletionErrorMessage = null) }
    }

}