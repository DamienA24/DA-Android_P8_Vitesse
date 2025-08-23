package com.quizocr.vitesse.domain.usecase

import com.quizocr.vitesse.data.repository.CandidateRepository
import com.quizocr.vitesse.data.repository.DataResult
import com.quizocr.vitesse.domain.model.Candidate
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

class GetAllCandidates  @Inject constructor(private val candidateRepository: CandidateRepository) {
    suspend fun execute(): Flow<DataResult<List<Candidate>>> {
        return candidateRepository.allCandidates()
    }
}