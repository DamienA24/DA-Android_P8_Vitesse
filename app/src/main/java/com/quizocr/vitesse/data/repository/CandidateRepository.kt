package com.quizocr.vitesse.data.repository

import android.util.Log
import com.quizocr.vitesse.data.dao.CandidateDao
import com.quizocr.vitesse.data.entity.CandidateEntity
import com.quizocr.vitesse.domain.model.Candidate
import com.quizocr.vitesse.domain.model.CandidateSummary
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.withContext

class CandidateRepository(private val candidateDao: CandidateDao) {

    /**
     * Fetch all candidates from the database.
     * @return Flow of DataResult containing a list of candidates or an error.
     * @see DataResult
     */
     fun allCandidates(): Flow<DataResult<List<CandidateSummary>>> {
        return candidateDao.getAllCandidates() //
            .map { summaryList ->
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

    /**
     * Update the favorite status of a candidate in the database.
     * @param candidateId The ID of the candidate to update.
     * @param isFavorite The new favorite status.
     */
    suspend fun updateFavoriteStatus(candidateId: Int, isFavorite: Boolean): DataResult<Unit>{
        return try {
            withContext(Dispatchers.IO) {
                candidateDao.updateFavoriteStatus(candidateId, isFavorite)
                DataResult.Success(Unit)
            }
        } catch (e: Exception) {
            DataResult.Error(Exception("Failed to update favorite status", e))
        }
    }

    /**
     * Deletes a specific candidate from the local database.
     * @param candidate The candidate to delete.
     * @return DataResult indicating success or failure.
     */
    suspend fun deleteCandidate(candidate: Candidate): DataResult<Unit> {
        return try {
            withContext(Dispatchers.IO) {
                val candidateEntity = CandidateEntity.fromDomain(candidate)
                candidateDao.deleteCandidate(candidateEntity)
                DataResult.Success(Unit)
            }
        } catch (e: Exception) {
            DataResult.Error(Exception("Failed to delete candidate", e))
        }
    }

    /**
     * Adds a new candidate to the local database.
     * @param candidate The candidate to add.
     * @return DataResult containing the ID of the newly added candidate or an error.
     */
    suspend fun addCandidate(candidate: Candidate): DataResult<Unit>  {
        return try {
            withContext(Dispatchers.IO) {
                val candidateEntity = CandidateEntity.fromDomain(candidate)
                val newId = candidateDao.insertCandidate(candidateEntity)
                DataResult.Success(newId)
            }
        } catch (e: Exception) {
            DataResult.Error(Exception("Failed to add candidate", e))
        }
    }

    /**
     * Updates an existing candidate in the local database.
     * @param candidate The candidate to update.
     * @return DataResult containing the number of rows updated or an error.
     */
    suspend fun updateCandidate(candidate: Candidate): DataResult<Unit> {
        return try {
            withContext(Dispatchers.IO) {
                val candidateEntity = CandidateEntity.fromDomain(candidate)
                val updatedRows = candidateDao.updateCandidate(candidateEntity)
                DataResult.Success(updatedRows)
            }
        } catch (e: Exception) {
            DataResult.Error(Exception("Failed to update candidate ${candidate.id}", e))
        }
    }
}



