package com.quizocr.vitesse.data.dao

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.quizocr.vitesse.data.entity.CandidateEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface CandidateDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertCandidate(candidate: CandidateEntity)

    @Update
    suspend fun updateCandidate(candidate: CandidateEntity)

    @Delete
    suspend fun deleteCandidate(candidate: CandidateEntity)

    @Query("SELECT * FROM candidates ORDER BY last_name ASC, first_name ASC")
    fun getAllCandidates(): Flow<List<CandidateEntity>>

    @Query("SELECT * FROM candidates WHERE is_favorite = 1 ORDER BY last_name ASC, first_name ASC")
    fun getFavoriteCandidates(): Flow<List<CandidateEntity>>

    @Query("""
        SELECT * FROM candidates
        WHERE LOWER(first_name) LIKE LOWER(:query) 
           OR LOWER(last_name) LIKE LOWER(:query)
        ORDER BY last_name ASC, first_name ASC
    """)
    fun searchCandidates(query: String): Flow<List<CandidateEntity>>

    @Query("UPDATE candidates SET is_favorite = :isFavorite WHERE id = :id")
    suspend fun updateFavoriteStatus(id: Int, isFavorite: Boolean)

}