package com.quizocr.vitesse.ui.resumeCandidate

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.quizocr.vitesse.data.repository.DataResult
import com.quizocr.vitesse.domain.model.Candidate
import com.quizocr.vitesse.domain.usecase.DeleteCandidateUseCase
import com.quizocr.vitesse.domain.usecase.GetCandidateById
import com.quizocr.vitesse.domain.usecase.GetCurrencyConversionRateUseCase
import com.quizocr.vitesse.domain.usecase.UpdateCandidateFavoriteStatusUseCase
import com.quizocr.vitesse.utils.formatCurrencyInUk
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class ResumeCandidateViewModel @Inject constructor(
    private val getCandidateById: GetCandidateById,
    private val getCurrencyConversionRateUseCase: GetCurrencyConversionRateUseCase,
    private val updateCandidateFavoriteStatusUseCase: UpdateCandidateFavoriteStatusUseCase,
    private val deleteCandidateUseCase: DeleteCandidateUseCase,
    savedStateHandle: SavedStateHandle
) : ViewModel() {

    private val _uiState = MutableStateFlow(ResumeCandidateUiState())
    val uiState: StateFlow<ResumeCandidateUiState> = _uiState.asStateFlow()
    val candidateId: Int = savedStateHandle["candidateId"] ?: -1
    private var salaryConversionLaunched = false

    init {
        if (candidateId > 0) {
            loadCandidateDetails(candidateId)
        } else {
            setError("Invalid Candidate ID.")
        }
    }

    private fun loadCandidateDetails(id: Int) {
        getCandidateById.execute(id)
            .onStart { setLoading() }
            .onEach { result ->
                when (result) {
                    is DataResult.Success -> handleCandidateLoaded(result.data)
                    is DataResult.Error -> setError(result.exception.message ?: "Failed to load candidate.")
                }
            }
            .launchIn(viewModelScope)
    }

    private fun handleCandidateLoaded(candidate: Candidate) {
        _uiState.update {
            it.copy(isLoading = !salaryConversionLaunched, candidate = candidate, errorMessage = null)
        }
        if (!salaryConversionLaunched && candidate.salaryEuros > 0) {
            convertSalaryToPounds(candidate.salaryEuros)
        }
    }

    private fun convertSalaryToPounds(salaryEuros: Double) {
        getCurrencyConversionRateUseCase("eur", "gbp")
            .onEach { result ->
                when (result) {
                    is DataResult.Success -> {
                        val salaryInPounds = salaryEuros * result.data.rate
                        _uiState.update {
                            it.copy(
                                isLoading = false,
                                formattedSalaryPounds = formatCurrencyInUk(salaryInPounds),
                                errorMessage = null
                            )
                        }
                        salaryConversionLaunched = true

                    }
                    is DataResult.Error -> {
                        setError(result.exception.message ?: "Currency conversion failed.")
                        _uiState.update { it.copy(isLoading = false) }
                        salaryConversionLaunched = true
                    }
                }
            }
            .launchIn(viewModelScope)
    }

    fun toggleFavoriteStatus() {
        val current = _uiState.value.candidate ?: return
        val newStatus = !current.isFavorite

        viewModelScope.launch {
            when (val res = updateCandidateFavoriteStatusUseCase.execute(current.id, newStatus)) {
                is DataResult.Success -> {
                    _uiState.update { it.copy(candidate = current.copy(isFavorite = newStatus)) }
                }

                is DataResult.Error -> {
                    _uiState.update { it.copy(errorMessage = res.exception.message) }
                }
            }
        }
    }

    fun deleteCandidate() {
        val candidate = _uiState.value.candidate ?: return

        viewModelScope.launch {
            _uiState.update { it.copy(isDeleting = true, errorMessage = null) }
            when (val result = deleteCandidateUseCase.execute(candidate)) {
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
                    _uiState.update { it.copy(isDeleting = false) }
                    setError(result.exception.message ?: "Failed to delete candidate.")
                }
            }
        }
    }

    fun onNavigationDone() {
        _uiState.update { it.copy(navigateBackAfterDeletion = false) }
    }

    private fun setLoading() {
        _uiState.update { it.copy(isLoading = true) }
    }

    private fun setError(message: String) {
        _uiState.update { it.copy(isLoading = false, errorMessage = message) }
    }
}
