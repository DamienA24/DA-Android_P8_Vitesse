package com.quizocr.vitesse.domain.usecase

import com.quizocr.vitesse.data.repository.CandidateRepository // Assurez-vous que ce chemin est correct
import com.quizocr.vitesse.domain.model.Candidate
import com.quizocr.vitesse.data.repository.DataResult // Assurez-vous que ce chemin est correct
import javax.inject.Inject

class UpdateCandidateUseCase @Inject constructor(
    private val candidateRepository: CandidateRepository
) {
    suspend fun execute(candidate: Candidate): DataResult<Unit> {
        if (candidate.id == 0) {
            return DataResult.Error(Exception("Candidate ID cannot be 0"))
        }
        return candidateRepository.updateCandidate(candidate)
    }
}
