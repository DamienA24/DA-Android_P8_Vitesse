package com.quizocr.vitesse.ui.addEditCandidate

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.quizocr.vitesse.domain.model.Candidate
import com.quizocr.vitesse.domain.usecase.GetCandidateById
import com.quizocr.vitesse.data.repository.DataResult
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
class AddEditCandidateViewModel @Inject constructor(
    private val getCandidateById: GetCandidateById,
    savedStateHandle: SavedStateHandle
) : ViewModel() {

    private val _uiState = MutableStateFlow(AddEditCandidateUiState())
    val uiState: StateFlow<AddEditCandidateUiState> = _uiState.asStateFlow()

    init {

        val candidateId: Int = savedStateHandle.get<Int>("candidateId") ?: -1
        val screenTitle: String = savedStateHandle.get<String>("screenTitle") ?: ""

        _uiState.update { it.copy(screenTitle = screenTitle) }

        if (candidateId != -1) {
            loadCandidateDetails(candidateId)
        }
    }

    private fun loadCandidateDetails(id: Int) {
        getCandidateById.execute(id)
            .onStart { _uiState.update { it.copy(isLoadingCandidateData = true) } }
            .onEach { result ->
                when (result) {
                    is DataResult.Success -> _uiState.update {
                        it.copy(
                            candidate = result.data,
                            isLoadingCandidateData = false,
                            errorMessage = null
                        )
                    }
                    is DataResult.Error -> _uiState.update {
                        it.copy(
                            isLoadingCandidateData = false,
                            errorMessage = result.exception.message ?: "Failed to load candidate"
                        )
                    }
                }
            }
            .launchIn(viewModelScope)
    }

    fun clearErrorMessage() {
        _uiState.update { it.copy(errorMessage = null) }
    }
}
