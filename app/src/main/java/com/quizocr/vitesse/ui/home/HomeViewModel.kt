package com.quizocr.vitesse.ui.home

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.quizocr.vitesse.data.repository.DataResult
import com.quizocr.vitesse.domain.model.Candidate
import com.quizocr.vitesse.domain.model.CandidateSummary
import com.quizocr.vitesse.domain.usecase.GetAllCandidates
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.FlowPreview
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.flow.onStart
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject
import kotlin.text.contains

@OptIn(FlowPreview::class)
@HiltViewModel
class HomeViewModel @Inject constructor(private val getAllCandidates: GetAllCandidates) :
    ViewModel() {

    private val _uiState = MutableStateFlow(CandidateUiState())
    val uiState: StateFlow<CandidateUiState> = _uiState.asStateFlow()

    private val _sourceCandidates = MutableStateFlow<List<CandidateSummary>>(emptyList())
    private val _searchQuery = MutableStateFlow("")

    private val _navigateToResumeEvent = MutableSharedFlow<CandidateSummary>()
    val navigateToResumeEvent: SharedFlow<CandidateSummary> = _navigateToResumeEvent.asSharedFlow()

    init {
        viewModelScope.launch {
            combine(_sourceCandidates, _searchQuery) { sourceList, query ->
                val filteredList = if (query.isBlank()) {
                    sourceList
                } else {
                    sourceList.filter { candidate ->
                        candidate.firstName.contains(query, ignoreCase = true) || candidate.lastName.contains(query, ignoreCase = true)
                    }
                }
                Pair(filteredList, query)
            }.collect { (filteredList, query) ->
                _uiState.update { currentState ->
                    currentState.copy(
                        candidates = filteredList,
                        searchQuery = query
                    )

                }
            }
        }
    }

    fun fetchAllCandidates() {
        getAllCandidates.execute()
            .onStart {
                _uiState.update { it.copy(isLoading = true, errorMessage = null) }
                 _sourceCandidates.value = emptyList()
            }
            .onEach { result: DataResult<List<CandidateSummary>> ->
                when (result) {
                    is DataResult.Success -> {
                        val candidates = result.data.sortedBy { it.lastName.lowercase() }
                        _sourceCandidates.value = candidates
                        _uiState.update { it.copy(isLoading = false, errorMessage = null) }
                    }
                    is DataResult.Error -> {
                        _uiState.update {
                            it.copy(
                                isLoading = false,
                                errorMessage = result.exception.message ?: "Unknown error"
                            )
                        }
                        _sourceCandidates.value = emptyList()
                    }
                }
            }
            .catch { e ->
                _uiState.update {
                    it.copy(
                        isLoading = false,
                        errorMessage = "Unexpected error during flow collection: ${e.message}"
                    )
                }
                _sourceCandidates.value = emptyList()
            }
            .launchIn(viewModelScope)
    }

    fun fetchAllCandidatesIfNeeded() {
        if (_sourceCandidates.value.isEmpty()) {
            fetchAllCandidates()
        }
    }
    fun setSearchQuery(query: String) {
        _searchQuery.value = query.trim()
    }
    fun clearErrorMessage() {
        _uiState.update { it.copy(errorMessage = null) }
    }

    fun onCandidateSelected(candidate: CandidateSummary) {
       viewModelScope.launch {
            _navigateToResumeEvent.emit(candidate)
        }
    }
}