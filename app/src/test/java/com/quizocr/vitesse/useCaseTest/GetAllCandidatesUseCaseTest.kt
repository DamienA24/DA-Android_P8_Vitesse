package com.quizocr.vitesse.useCaseTest

import com.quizocr.vitesse.data.repository.CandidateRepository
import com.quizocr.vitesse.data.repository.DataResult
import com.quizocr.vitesse.domain.model.CandidateSummary
import com.quizocr.vitesse.domain.usecase.GetAllCandidates
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.runTest
import org.junit.Assert
import org.junit.Before
import org.junit.Test
import org.mockito.kotlin.mock
import org.mockito.kotlin.whenever

/**
 * Example local unit test, which will execute on the development machine (host).
 *
 * See [testing documentation](http://d.android.com/tools/testing).
 */
class GetAllCandidatesUseCaseTest {

    private lateinit var candidateRepository: CandidateRepository
    private lateinit var getAllCandidatesUseCaseTest: GetAllCandidates

    @Before
    fun setUp() {
        candidateRepository = mock()
        getAllCandidatesUseCaseTest = GetAllCandidates(candidateRepository)
    }

    @Test
    fun `execute should return Success with a list of candidates when repository return success`() =
        runTest {
            // Arrange
            val expectedCandidates = listOf(
                CandidateSummary(
                    id = 1,
                    photoUri = "https://cdn.jsdelivr.net/gh/faker-js/assets-person-portrait/female/512/72.jpg", // ou votre URI
                    firstName = "John",
                    lastName = "Wick",
                    notes = "Lorem ipsum dolor sit amet, consectetur adipiscing elit. Aliquam eleifend volutpat scelerisque. Vestibulum tincidunt mauris purus, bibendum tincidunt est viverra non. Maecenas eget nunc diam. Cras enim urna, dictum at ex eget, pulvinar lobortis enim. Nullam nec turpis eros. Etiam consectetur nunc justo, ut rutrum ligula ornare a. Fusce augue velit, ornare quis imperdiet ut, vehicula venenatis ante. Nulla at accumsan velit. Nullam venenatis rhoncus augue eu imperdiet. Sed aliquet neque ac ante porta semper.",
                    isFavorite = false,
                ),
                CandidateSummary(
                    id = 2,
                    photoUri = "https://cdn.jsdelivr.net/gh/faker-js/assets-person-portrait/female/512/72.jpg", // ou votre URI
                    firstName = "John",
                    lastName = "Wick",
                    notes = "Lorem ipsum dolor sit amet, consectetur adipiscing elit. Aliquam eleifend volutpat scelerisque. Vestibulum tincidunt mauris purus, bibendum tincidunt est viverra non. Maecenas eget nunc diam. Cras enim urna, dictum at ex eget, pulvinar lobortis enim. Nullam nec turpis eros. Etiam consectetur nunc justo, ut rutrum ligula ornare a. Fusce augue velit, ornare quis imperdiet ut, vehicula venenatis ante. Nulla at accumsan velit. Nullam venenatis rhoncus augue eu imperdiet. Sed aliquet neque ac ante porta semper.",
                    isFavorite = false,
                )
            )


            val successResult = DataResult.Success(expectedCandidates)
            val flowResult = flowOf(successResult)

            whenever(candidateRepository.allCandidates()).thenReturn(flowResult)

            // Act
            val result = getAllCandidatesUseCaseTest.execute().first()

            // Assert
            Assert.assertTrue(result is DataResult.Success)
            Assert.assertEquals(expectedCandidates, (result as DataResult.Success).data)
        }

    @Test
    fun `execute should return Success with empty list when repository returns success with empty list`() =
        runTest {
            // Arrange
            val expectedCandidates = emptyList<CandidateSummary>()
            val successResult = DataResult.Success(expectedCandidates)
            val flowResult = flowOf(successResult)

            whenever(candidateRepository.allCandidates()).thenReturn(flowResult)

            // Act
            val result = getAllCandidatesUseCaseTest.execute()

            // Assert
            Assert.assertTrue(result.first() is DataResult.Success)
            Assert.assertEquals(expectedCandidates, (result.first() as DataResult.Success).data)
        }

    @Test
    fun `execute should return Error when repository returns error`() = runTest {

        // Arrange
        val expectedException = "Database connection failed"
        val errorResult = DataResult.Error(Exception(expectedException))

        val flowResult = flowOf(errorResult)
        whenever(candidateRepository.allCandidates()).thenReturn(flowResult)
        // Act
        val result = getAllCandidatesUseCaseTest.execute().first()
        // Assert
        Assert.assertTrue(result is DataResult.Error)
        Assert.assertEquals(expectedException, (result as DataResult.Error).exception.message)
    }
}