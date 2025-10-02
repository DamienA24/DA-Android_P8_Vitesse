package com.quizocr.vitesse.useCaseTest

import com.quizocr.vitesse.data.repository.CandidateRepository
import com.quizocr.vitesse.data.repository.DataResult
import com.quizocr.vitesse.domain.model.Candidate
import com.quizocr.vitesse.domain.usecase.UpdateCandidateUseCase
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
import java.time.LocalDate

class UpdateCandidateUseCaseTest {

    private lateinit var mockCandidateRepository: CandidateRepository
    private lateinit var updateCandidateUseCase: UpdateCandidateUseCase

    // Test data for an existing candidate to be updated
    private val candidateToUpdate = Candidate(
        id = 1, // ID must be non-zero for an update, as per use case logic
        firstName = "Bruce",
        lastName = "Wayne",
        phoneNumber = "555-BATMAN",
        email = "bruce.wayne@example.com",
        dateOfBirth = LocalDate.of(1972, 2, 19),
        salaryEuros = 1000000.0, // A modest salary
        notes = "Works at night.",
        photoUri = "uri://bat_signal",
        isFavorite = true,
        createdAt = System.currentTimeMillis() - 100000,
        updatedAt = System.currentTimeMillis() // This will be updated by the use case logic implicitly via repository
    )

    @Before
    fun setUp() {
        mockCandidateRepository = mock()
        updateCandidateUseCase = UpdateCandidateUseCase(mockCandidateRepository)
    }

    @Test
    fun `execute with valid candidate (non-zero ID) should call repository updateCandidate and return Success`() = runTest {
        // Arrange
        // Mock the repository to return success when updateCandidate is called
        whenever(mockCandidateRepository.updateCandidate(candidateToUpdate))
            .thenReturn(DataResult.Success(Unit)) // updateCandidate returns DataResult<Unit>

        // Act
        val result = updateCandidateUseCase.execute(candidateToUpdate)

        // Assert
        assertTrue("Result should be Success", result is DataResult.Success)
        // Verify that the repository's updateCandidate method was called exactly once with the correct candidate
        verify(mockCandidateRepository).updateCandidate(candidateToUpdate)
    }

    @Test
    fun `execute with candidate having zero ID should return Error and not call repository`() = runTest {
        // Arrange
        val candidateWithZeroId = candidateToUpdate.copy(id = 0) // Invalid ID for an update

        // Act
        val result = updateCandidateUseCase.execute(candidateWithZeroId)

        // Assert
        assertTrue("Result should be Error for zero ID", result is DataResult.Error)
        // The use case currently returns a generic Exception, not IllegalArgumentException specifically
        // assertEquals("Candidate ID cannot be 0", (result as DataResult.Error).exception.message)
        // Let's check the type of exception if needed, or just the message if it's always "Candidate ID cannot be 0"
        assertTrue("Exception message should indicate ID cannot be 0",
            (result as DataResult.Error).exception.message?.contains("Candidate ID cannot be 0") == true
        )


        // Verify that the repository's updateCandidate method was NEVER called
        verify(mockCandidateRepository, never()).updateCandidate(any())
    }

    @Test
    fun `execute when repository updateCandidate fails should return Error`() = runTest {
        // Arrange
        val repositoryErrorMessage = "Database update failed"
        // Mock the repository to return an error when updateCandidate is called
        whenever(mockCandidateRepository.updateCandidate(candidateToUpdate))
            .thenReturn(DataResult.Error(Exception(repositoryErrorMessage)))

        // Act
        val result = updateCandidateUseCase.execute(candidateToUpdate)

        // Assert
        assertTrue("Result should be Error when repository fails", result is DataResult.Error)
        assertEquals(
            "Exception message should match repository's error message",
            repositoryErrorMessage,
            (result as DataResult.Error).exception.message
        )
        // Verify that the repository's updateCandidate method was called
        verify(mockCandidateRepository).updateCandidate(candidateToUpdate)
    }
}

