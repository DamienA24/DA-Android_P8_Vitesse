package com.quizocr.vitesse.domain.usecase

import com.quizocr.vitesse.data.repository.CandidateRepository
import com.quizocr.vitesse.domain.model.Candidate
import com.quizocr.vitesse.data.repository.DataResult
import javax.inject.Inject

class AddNewCandidateUseCase @Inject constructor(
    private val candidateRepository: CandidateRepository
) {
    suspend fun execute(candidate: Candidate): DataResult<Unit> {

        if (candidate.id != 0) {
            return DataResult.Error(IllegalArgumentException("Candidate ID should be 0 for new candidates."))
        }
        return candidateRepository.addCandidate(candidate)
    }
}
