package com.quizocr.vitesse.domain.usecase

import com.quizocr.vitesse.data.repository.CandidateRepository
import com.quizocr.vitesse.data.repository.DataResult
import com.quizocr.vitesse.domain.model.Candidate
import javax.inject.Inject

class DeleteCandidateUseCase @Inject constructor(
    private val candidateRepository: CandidateRepository
) {
    suspend fun execute(candidate: Candidate): DataResult<Unit> {
        return candidateRepository.deleteCandidate(candidate)
    }
}