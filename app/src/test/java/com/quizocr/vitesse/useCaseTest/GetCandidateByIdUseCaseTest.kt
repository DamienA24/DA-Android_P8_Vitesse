package com.quizocr.vitesse.useCaseTest

import app.cash.turbine.test
import com.quizocr.vitesse.data.repository.CandidateRepository
import com.quizocr.vitesse.data.repository.DataResult
import com.quizocr.vitesse.domain.model.Candidate
import com.quizocr.vitesse.domain.usecase.GetCandidateById
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import org.mockito.kotlin.mock
import org.mockito.kotlin.whenever
import java.time.LocalDate

@ExperimentalCoroutinesApi
class GetCandidateByIdUseCaseTest {

    private lateinit var mockCandidateRepository: CandidateRepository
    private lateinit var getCandidateByIdUseCase: GetCandidateById

    private val testCandidateId = 1
    private val mockCandidate = Candidate(
        id = testCandidateId,
        firstName = "Jane",
        lastName = "Doe",
        phoneNumber = "987654321",
        email = "jane.doe@example.com",
        dateOfBirth = LocalDate.of(1992, 2, 2),
        salaryEuros = 60000.0,
        notes = "notes",
        isFavorite = true,
        createdAt = System.currentTimeMillis(),
        updatedAt = System.currentTimeMillis()
    )

    @Before
    fun setUp() {
        mockCandidateRepository = mock()
        getCandidateByIdUseCase = GetCandidateById(mockCandidateRepository)
    }

    @Test
    fun `execute should call repository and return success result`() = runTest {
        // Arrange:
        val successFlow = flowOf(DataResult.Success(mockCandidate))
        whenever(mockCandidateRepository.getCandidateById(testCandidateId)).thenReturn(successFlow)

        // Act:
        val resultFlow = getCandidateByIdUseCase.execute(testCandidateId)

        // Assert:
        resultFlow.test {
            val result = awaitItem()
            assertTrue("Result should be Success", result is DataResult.Success)
            assertEquals(mockCandidate, (result as DataResult.Success).data)
            awaitComplete()
        }

    }

    @Test
    fun `execute should call repository and return error result when repository fails`() = runTest {
        // Arrange:
        val errorMessage = "Database error"
        val exception = Exception(errorMessage)
        val errorFlow = flowOf(DataResult.Error(exception) as DataResult<Candidate>) // Cast nécessaire car DataResult.Error n'est pas paramétré par T directement
        whenever(mockCandidateRepository.getCandidateById(testCandidateId)).thenReturn(errorFlow)

        // Act:
        val resultFlow = getCandidateByIdUseCase.execute(testCandidateId)

        // Assert:
        resultFlow.test {
            val result = awaitItem()
            assertTrue("Result should be Error", result is DataResult.Error)
            assertEquals(errorMessage, (result as DataResult.Error).exception.message)
            awaitComplete()
        }
    }

    @Test
    fun `execute should call repository and return error result for candidate not found`() = runTest {
        // Arrange:
        val notFoundException = NoSuchElementException("Candidate not found with id: $testCandidateId")
        val notFoundFlow = flowOf(DataResult.Error(notFoundException) as DataResult<Candidate>)
        whenever(mockCandidateRepository.getCandidateById(testCandidateId)).thenReturn(notFoundFlow)

        // Act
        val resultFlow = getCandidateByIdUseCase.execute(testCandidateId)

        // Assert
        resultFlow.test {
            val result = awaitItem()
            assertTrue(result is DataResult.Error)
            assertTrue((result as DataResult.Error).exception is NoSuchElementException)
            assertEquals("Candidate not found with id: $testCandidateId", result.exception.message)
            awaitComplete()
        }
    }
}
