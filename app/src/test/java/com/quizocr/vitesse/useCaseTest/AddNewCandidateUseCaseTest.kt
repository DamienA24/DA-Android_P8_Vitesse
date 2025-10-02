package com.quizocr.vitesse.useCaseTest

import com.quizocr.vitesse.data.repository.CandidateRepository
import com.quizocr.vitesse.data.repository.DataResult
import com.quizocr.vitesse.domain.model.Candidate
import com.quizocr.vitesse.domain.usecase.AddNewCandidateUseCase
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

class AddNewCandidateUseCaseTest {

    private lateinit var mockCandidateRepository: CandidateRepository
    private lateinit var addNewCandidateUseCase: AddNewCandidateUseCase

    // Test data for a new candidate
    private val newCandidateToAdd = Candidate(
        id = 0, // ID must be 0 for a new candidate, as per use case logic
        firstName = "Sarah",
        lastName = "Connor",
        phoneNumber = "555-0199",
        email = "sarah.connor@example.com",
        dateOfBirth = LocalDate.of(1985, 5, 5),
        salaryEuros = 75000.0,
        notes = "Future leader of the resistance.",
        photoUri = "uri://sarah_connor_photo",
        isFavorite = false,
        createdAt = System.currentTimeMillis(),
        updatedAt = System.currentTimeMillis()
    )

    @Before
    fun setUp() {
        mockCandidateRepository = mock()
        addNewCandidateUseCase = AddNewCandidateUseCase(mockCandidateRepository)
    }

    @Test
    fun `execute with valid new candidate (ID 0) should call repository addCandidate and return Success`() = runTest {
        // Arrange
        // Mock the repository to return success when addCandidate is called
        whenever(mockCandidateRepository.addCandidate(newCandidateToAdd))
            .thenReturn(DataResult.Success(Unit)) // addCandidate returns DataResult<Unit>

        // Act
        val result = addNewCandidateUseCase.execute(newCandidateToAdd)

        // Assert
        assertTrue("Result should be Success", result is DataResult.Success)
        // Verify that the repository's addCandidate method was called exactly once with the correct candidate
        verify(mockCandidateRepository).addCandidate(newCandidateToAdd)
    }

    @Test
    fun `execute with candidate having non-zero ID should return Error and not call repository`() = runTest {
        // Arrange
        val candidateWithInvalidId = newCandidateToAdd.copy(id = 1) // Invalid ID for a new candidate

        // Act
        val result = addNewCandidateUseCase.execute(candidateWithInvalidId)

        // Assert
        assertTrue("Result should be Error for non-zero ID", result is DataResult.Error)
        assertTrue("Error should be IllegalArgumentException", (result as DataResult.Error).exception is IllegalArgumentException)
        assertEquals(
            "Candidate ID should be 0 for new candidates.",
            (result.exception as IllegalArgumentException).message
        )
        // Verify that the repository's addCandidate method was NEVER called
        verify(mockCandidateRepository, never()).addCandidate(any())
    }

    @Test
    fun `execute when repository addCandidate fails should return Error`() = runTest {
        // Arrange
        val repositoryErrorMessage = "Database insertion failed"
        // Mock the repository to return an error when addCandidate is called
        whenever(mockCandidateRepository.addCandidate(newCandidateToAdd))
            .thenReturn(DataResult.Error(Exception(repositoryErrorMessage)))

        // Act
        val result = addNewCandidateUseCase.execute(newCandidateToAdd)

        // Assert
        assertTrue("Result should be Error when repository fails", result is DataResult.Error)
        assertEquals(
            "Exception message should match repository's error message",
            repositoryErrorMessage,
            (result as DataResult.Error).exception.message
        )
        // Verify that the repository's addCandidate method was called
        verify(mockCandidateRepository).addCandidate(newCandidateToAdd)
    }
}
