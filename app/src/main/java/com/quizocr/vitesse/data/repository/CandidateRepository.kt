package com.quizocr.vitesse.data.repository

import android.util.Log
import com.quizocr.vitesse.data.dao.CandidateDao
import com.quizocr.vitesse.data.entity.CandidateEntity
import com.quizocr.vitesse.domain.model.Candidate
import com.quizocr.vitesse.domain.model.CandidateSummary
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.map

class CandidateRepository(private val candidateDao: CandidateDao) {

    /**
     * Fetch all candidates from the database.
     * @return Flow of DataResult containing a list of candidates or an error.
     * @see DataResult
     */
     fun allCandidates(): Flow<DataResult<List<CandidateSummary>>> {
        return candidateDao.getAllCandidates() //
            .map { summaryList ->
                Log.d(
                    "CandidateRepository",
                    "Fetched candidate summaries from database: $summaryList"
                )
                DataResult.Success(summaryList) as DataResult<List<CandidateSummary>>
            }
            .catch { e ->
                emit(DataResult.Error(Exception("Failed to fetch sleeps from database", e)))
            }
    }

    /**
     * Fetch a specific candidate by their ID from the database.
     * @param id The ID of the candidate to fetch.
     * @return Flow of DataResult containing the candidate or an error.
     * @see DataResult
     */
    fun getCandidateById(id: Int): Flow<DataResult<Candidate>> {
        return candidateDao.getCandidateById(id)
            .map { entity: CandidateEntity? ->
                if (entity != null) {
                    try {
                        val domainModel = Candidate.fromEntity(entity)
                        DataResult.Success(domainModel)
                    } catch (mappingException: Exception) {
                        DataResult.Error(Exception("Failed to map data for candidate $id", mappingException)) as DataResult<Candidate>
                    }
                } else {
                    DataResult.Error(NoSuchElementException("Candidate not found with id: $id")) as DataResult<Candidate>
                }
            }.catch { e ->
                emit(DataResult.Error(Exception("Failed to fetch candidate data from database", e)))
            }
    }

}



