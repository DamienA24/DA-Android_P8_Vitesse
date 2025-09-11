package com.quizocr.vitesse.domain.usecase

import com.quizocr.vitesse.data.repository.CandidateRepository
import com.quizocr.vitesse.data.repository.DataResult
import com.quizocr.vitesse.domain.model.Candidate
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

class GetCandidateById  @Inject constructor(private val candidateRepository: CandidateRepository) {
     fun execute(id: Int): Flow<DataResult<Candidate>> {
        return candidateRepository.getCandidateById(id)
    }
}