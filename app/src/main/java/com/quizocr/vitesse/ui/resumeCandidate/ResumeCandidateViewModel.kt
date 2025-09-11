package com.quizocr.vitesse.ui.resumeCandidate

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.quizocr.vitesse.data.repository.DataResult
import com.quizocr.vitesse.domain.usecase.GetCandidateById
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.flow.onStart
import kotlinx.coroutines.flow.update
import javax.inject.Inject

@HiltViewModel
class ResumeCandidateViewModel @Inject constructor(
    private val getCandidateById: GetCandidateById,
    private val savedStateHandle: SavedStateHandle) : ViewModel() {

    private val _uiState = MutableStateFlow(ResumeCandidateUiState())
    val uiState: StateFlow<ResumeCandidateUiState> = _uiState.asStateFlow()

    private val candidateId: Int = savedStateHandle.get<Int>("candidateId") ?: -1

    init {
        if (candidateId != -1 && candidateId != 0) { // Vérifiez une valeur d'ID valide
            loadCandidateDetails(candidateId)
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
                _uiState.update { it.copy(isLoading = true, errorMessage = null, candidate = null) }
            }
            .onEach { result ->
                _uiState.update { currentState ->
                    when (result) {
                        is DataResult.Success -> {
                            currentState.copy(
                                isLoading = false,
                                candidate = result.data,
                                errorMessage = null
                            )
                        }
                        is DataResult.Error -> {
                            currentState.copy(
                                isLoading = false,
                                candidate = null,
                                errorMessage = result.exception.message ?: "An unknown error occurred"
                            )
                        }
                    }
                }
            }
            .launchIn(viewModelScope)
    }

}