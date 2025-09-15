package com.quizocr.vitesse.domain.usecase

import com.quizocr.vitesse.data.repository.CandidateRepository
import com.quizocr.vitesse.data.repository.DataResult
import javax.inject.Inject

class UpdateCandidateFavoriteStatusUseCase @Inject constructor(
    private val candidateRepository: CandidateRepository
) {
    suspend fun execute(candidateId: Int, isFavorite: Boolean): DataResult<Unit> {
        if (candidateId <= 0) {
            return DataResult.Error(IllegalArgumentException("Invalid Candidate ID"))
        }
        return candidateRepository.updateFavoriteStatus(candidateId, isFavorite)
    }
}