package com.quizocr.vitesse.useCaseTest

import com.quizocr.vitesse.data.repository.CandidateRepository
import com.quizocr.vitesse.data.repository.DataResult
import com.quizocr.vitesse.domain.usecase.UpdateCandidateFavoriteStatusUseCase
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import org.mockito.kotlin.any
import org.mockito.kotlin.mock
import org.mockito.kotlin.never
import org.mockito.kotlin.verify
import org.mockito.kotlin.whenever

@ExperimentalCoroutinesApi
class UpdateCandidateFavoriteStatusUseCaseTest {

    private lateinit var mockCandidateRepository: CandidateRepository
    private lateinit var updateCandidateFavoriteStatusUseCase: UpdateCandidateFavoriteStatusUseCase

    @Before
    fun setUp() {
        mockCandidateRepository = mock()
        updateCandidateFavoriteStatusUseCase = UpdateCandidateFavoriteStatusUseCase(mockCandidateRepository)
    }

    @Test
    fun `execute when candidateId is valid and repository succeeds then returns Success`() = runTest {
        // Arrange
        val validCandidateId = 1
        val isFavoriteStatus = true
        whenever(mockCandidateRepository.updateFavoriteStatus(validCandidateId, isFavoriteStatus))
            .thenReturn(DataResult.Success(Unit))

        // Act
        val result = updateCandidateFavoriteStatusUseCase.execute(validCandidateId, isFavoriteStatus)

        // Assert
        assertTrue("Result should be Success", result is DataResult.Success)
        verify(mockCandidateRepository).updateFavoriteStatus(validCandidateId, isFavoriteStatus)
    }

    @Test
    fun `execute when candidateId is valid and repository fails then returns Error`() = runTest {
        // Arrange
        val validCandidateId = 1
        val isFavoriteStatus = false
        val errorMessage = "Database update failed"
        val exception = Exception(errorMessage)

        whenever(mockCandidateRepository.updateFavoriteStatus(validCandidateId, isFavoriteStatus))
            .thenReturn(DataResult.Error(exception))

        // Act
        val result = updateCandidateFavoriteStatusUseCase.execute(validCandidateId, isFavoriteStatus)

        // Assert
        assertTrue("Result should be Error", result is DataResult.Error)
        assertEquals(errorMessage, (result as DataResult.Error).exception.message)
        verify(mockCandidateRepository).updateFavoriteStatus(validCandidateId, isFavoriteStatus)
    }

    @Test
    fun `execute when candidateId is zero then returns Error and does not call repository`() = runTest {
        // Arrange
        val invalidCandidateId = 0
        val isFavoriteStatus = true

        // Act
        val result = updateCandidateFavoriteStatusUseCase.execute(invalidCandidateId, isFavoriteStatus)

        // Assert
        assertTrue("Result should be Error for zero ID", result is DataResult.Error)
        assertTrue("Exception should be IllegalArgumentException for zero ID", (result as DataResult.Error).exception is IllegalArgumentException)
        assertEquals("Invalid Candidate ID", result.exception.message)

        verify(mockCandidateRepository, never()).updateFavoriteStatus(any(), any())
    }

    @Test
    fun `execute when candidateId is negative then returns Error and does not call repository`() = runTest {
        // Arrange
        val invalidCandidateId = -5
        val isFavoriteStatus = false

        // Act
        val result = updateCandidateFavoriteStatusUseCase.execute(invalidCandidateId, isFavoriteStatus)

        // Assert
        assertTrue("Result should be Error for negative ID", result is DataResult.Error)
        assertTrue("Exception should be IllegalArgumentException for negative ID", (result as DataResult.Error).exception is IllegalArgumentException)
        assertEquals("Invalid Candidate ID", result.exception.message)

        verify(mockCandidateRepository, never()).updateFavoriteStatus(any(), any())
    }
}

