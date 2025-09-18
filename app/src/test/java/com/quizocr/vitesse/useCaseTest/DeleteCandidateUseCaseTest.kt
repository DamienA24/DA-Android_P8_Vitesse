package com.quizocr.vitesse.useCaseTest

import com.quizocr.vitesse.data.repository.CandidateRepository
import com.quizocr.vitesse.data.repository.DataResult
import com.quizocr.vitesse.domain.model.Candidate
import com.quizocr.vitesse.domain.usecase.DeleteCandidateUseCase
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import org.mockito.kotlin.mock
import org.mockito.kotlin.verify
import org.mockito.kotlin.whenever
import java.time.LocalDate

@ExperimentalCoroutinesApi
class DeleteCandidateUseCaseTest {

    private lateinit var mockCandidateRepository: CandidateRepository
    private lateinit var deleteCandidateUseCase: DeleteCandidateUseCase

    private val testCandidate = Candidate(
        id = 1,
        firstName = "Candidate",
        lastName = "ToDelete",
        phoneNumber = "12345",
        email = "delete@example.com",
        dateOfBirth = LocalDate.of(2000, 1, 1),
        salaryEuros = 1000.0,
        isFavorite = false,
        notes = "Notes for deletion",
        createdAt = System.currentTimeMillis(),
        updatedAt = System.currentTimeMillis()
    )

    @Before
    fun setUp() {
        mockCandidateRepository = mock()
        deleteCandidateUseCase = DeleteCandidateUseCase(mockCandidateRepository)
    }

    @Test
    fun `execute should call repository deleteCandidate and return Success when repository succeeds`() = runTest {
        // Arrange
        // Configure the mock repository to return a success on deletion
        whenever(mockCandidateRepository.deleteCandidate(testCandidate))
            .thenReturn(DataResult.Success(Unit))

        // Act
        val result = deleteCandidateUseCase.execute(testCandidate)

        // Assert
        assertTrue("Result should be Success", result is DataResult.Success)
        // Verify that the correct repository method was called with the correct candidate
        verify(mockCandidateRepository).deleteCandidate(testCandidate)
    }

    @Test
    fun `execute should call repository deleteCandidate and return Error when repository fails`() = runTest {
        // Arrange
        val errorMessage = "Failed to delete candidate from database"
        val exception = Exception(errorMessage)

        whenever(mockCandidateRepository.deleteCandidate(testCandidate))
            .thenReturn(DataResult.Error(exception))

        // Act
        val result = deleteCandidateUseCase.execute(testCandidate)

        // Assert
        assertTrue("Result should be Error", result is DataResult.Error)
        assertEquals(errorMessage, (result as DataResult.Error).exception.message)

        verify(mockCandidateRepository).deleteCandidate(testCandidate)
    }
}

