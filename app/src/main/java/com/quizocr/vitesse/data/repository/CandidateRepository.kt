package com.quizocr.vitesse.data.repository

import com.quizocr.vitesse.data.dao.CandidateDao
import com.quizocr.vitesse.domain.model.Candidate
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.map

class CandidateRepository(private val candidateDao: CandidateDao) {

    /**
     * Fetch all candidates from the database.
     * @return Flow of DataResult containing a list of candidates or an error.
     * @see DataResult
     */
  suspend fun getAllCandidates(): Flow<DataResult<List<Candidate>>> {
        return candidateDao.getAllCandidates()
            .map { entityList ->
                val domainList = entityList.map { entity -> Candidate.fromEntity(entity) }
                DataResult.Success(domainList) as DataResult<List<Candidate>>
            }
            .catch { e ->
                emit(DataResult.Error(Exception("Failed to fetch candidates from database", e)))
            }
    }

    suspend fun getFavoriteCandidates(): Flow<DataResult<List<Candidate>>> {
        return candidateDao.getFavoriteCandidates()
            .map { entityList ->
                val domainList = entityList.map { entity -> Candidate.fromEntity(entity) }
                DataResult.Success(domainList) as DataResult<List<Candidate>>
            }
            .catch { e ->
                emit(DataResult.Error(Exception("Failed to fetch favorite candidates from database", e)))
            }
    }
}


