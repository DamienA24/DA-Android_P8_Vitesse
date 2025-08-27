package com.quizocr.vitesse.ui.home

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.quizocr.vitesse.data.repository.DataResult
import com.quizocr.vitesse.domain.model.Candidate
import com.quizocr.vitesse.domain.usecase.GetAllCandidates
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class HomeViewModel @Inject constructor(private val getAllCandidates: GetAllCandidates) :
    ViewModel() {

        private val _uiState = MutableStateFlow(CandidateUiState())
        val uiState: StateFlow<CandidateUiState> = _uiState.asStateFlow()

    private val _allCandidates = MutableStateFlow<List<Candidate>>(emptyList())
    val allCandidates: StateFlow<List<Candidate>> = _allCandidates.asStateFlow()

    private val _errorMessage = MutableStateFlow<String?>(null)
    val errorMessage: StateFlow<String?> = _errorMessage.asStateFlow()

    fun fetchAllCandidates() {
        viewModelScope.launch {
            _uiState.update { currentState -> currentState.copy(isLoading = true) }
            getAllCandidates.execute()
                .catch { e ->
                    _errorMessage.value = "Unexpected error in sleep data flow: ${e.message}"
                    _allCandidates.value = emptyList()
                }
                .collect { result ->
                    when (result) {
                        is DataResult.Success -> {
                            Log.d("HomeViewModel", "Fetched all candidates: ${result.data}")
                            _uiState.update{ currentState ->
                                currentState.copy(isLoading = false, candidates = result.data) }
                            _allCandidates.value = result.data
                            _errorMessage.value = null
                        }
                        is DataResult.Error -> {
                            Log.e("HomeViewModel", "Failed to load all candidates", result.exception)
                            _uiState.update { currentState ->
                                currentState.copy(isLoading = false, errorMessage = result.exception.message)
                            }
                            _allCandidates.value = emptyList()
                            _errorMessage.value = "Failed to load all candidates: ${result.exception.message}"
                        }
                    }
                }
        }
    }

    fun clearErrorMessage() {
        _uiState.update { it.copy(errorMessage = null) }
    }
}