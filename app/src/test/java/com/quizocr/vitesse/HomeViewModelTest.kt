package com.quizocr.vitesse

import app.cash.turbine.test
import com.quizocr.vitesse.data.repository.DataResult
import com.quizocr.vitesse.domain.model.Candidate
import com.quizocr.vitesse.domain.usecase.GetAllCandidates
import com.quizocr.vitesse.ui.home.HomeViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.*
import org.junit.Assert
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.rules.TestWatcher
import org.junit.runner.Description
import org.mockito.kotlin.mock
import org.mockito.kotlin.times
import org.mockito.kotlin.verify
import org.mockito.kotlin.whenever
import java.time.LocalDate

@ExperimentalCoroutinesApi
class HomeViewModelTest {

    @get:Rule
    val mainDispatcherRule = MainDispatcherRule()

    private lateinit var getAllCandidatesUseCase: GetAllCandidates
    private lateinit var viewModel: HomeViewModel

    private val testDate = LocalDate.of(2024, 1, 15)
    private val candidate1 = Candidate(1, "", "Alice", "Smith", "111", "alice@example.com", testDate, 60000.0, "", false, 0, 0)
    private val candidate2 = Candidate(2, "", "Bob", "Johnson", "222", "bob@example.com", testDate.plusYears(1), 70000.0, "", true, 0, 0)
    private val candidate3 = Candidate(3, "", "Carol", "Smith", "333", "carol@example.com", testDate.plusYears(2), 65000.0, "", false, 0, 0)
    private val mockCandidateList = listOf(candidate1, candidate2, candidate3)

    @Before
    fun setUp() {
        getAllCandidatesUseCase = mock()
        viewModel = HomeViewModel(getAllCandidatesUseCase)
    }

    @Test
    fun `fetchAllCandidates should update uiState with loading then success`() = runTest {
        whenever(getAllCandidatesUseCase.execute()).thenReturn(flowOf(DataResult.Success(mockCandidateList)))

        viewModel.uiState.test {
            val initialState = awaitItem()
            assertTrue(initialState.candidates.isEmpty())
            assertNull(initialState.errorMessage)
            Assert.assertFalse(initialState.isLoading)

            viewModel.fetchAllCandidates()

            val loadingState = awaitItem()
            assertTrue(loadingState.isLoading)
            assertEquals(initialState.candidates, loadingState.candidates)

            val intermediateSuccessState = awaitItem() // isLoading=false, candidates still from loadingState
            Assert.assertFalse(intermediateSuccessState.isLoading)
            assertNull(intermediateSuccessState.errorMessage)
            assertEquals(loadingState.candidates, intermediateSuccessState.candidates)

            val finalSuccessState = awaitItem() // candidates populated by combine
            Assert.assertFalse(finalSuccessState.isLoading)
            assertNull(finalSuccessState.errorMessage)
            assertEquals(mockCandidateList, finalSuccessState.candidates)

            cancelAndConsumeRemainingEvents()
        }
    }

    @Test
    fun `fetchAllCandidates should update uiState with loading then error from DataResult`() = runTest {
        val errorMessage = "Network error"
        whenever(getAllCandidatesUseCase.execute()).thenReturn(flowOf(DataResult.Error(Exception(errorMessage))))

        viewModel.uiState.test {
            val initialState = awaitItem() // Initial state
            Assert.assertFalse(initialState.isLoading)
            assertTrue(initialState.candidates.isEmpty())

            viewModel.fetchAllCandidates()

            assertEquals(true, awaitItem().isLoading) // Loading state

            val errorState = awaitItem() // Error state
            Assert.assertFalse(errorState.isLoading)
            assertEquals(errorMessage, errorState.errorMessage)
            assertTrue(errorState.candidates.isEmpty())

            cancelAndConsumeRemainingEvents()
        }
    }

    @Test
    fun `fetchAllCandidates should update uiState with loading then error from flow catch`() = runTest {
        val exceptionMessage = "Flow collection error"
        whenever(getAllCandidatesUseCase.execute()).thenReturn(flow { throw Exception(exceptionMessage) })

        viewModel.uiState.test {
            awaitItem() // Initial state

            viewModel.fetchAllCandidates()

            assertEquals(true, awaitItem().isLoading) // Loading state

            val errorState = awaitItem() // Error state from catch block
            Assert.assertFalse(errorState.isLoading)
            assertEquals("Unexpected error: $exceptionMessage", errorState.errorMessage)
            assertTrue(errorState.candidates.isEmpty())

            cancelAndConsumeRemainingEvents()
        }
    }

    private suspend fun TestScope.setupViewModelWithCandidates() {
        whenever(getAllCandidatesUseCase.execute()).thenReturn(flowOf(DataResult.Success(mockCandidateList)))
        viewModel.fetchAllCandidates()

        viewModel.uiState.test {
            awaitItem() // initial
            awaitItem() // loading
            awaitItem() // intermediate success
            awaitItem() // final success (candidates loaded)
            cancelAndConsumeRemainingEvents()
        }
        advanceUntilIdle()
    }


    @Test
    fun `setSearchQuery should update uiState with filtered candidates`() = runTest {
        setupViewModelWithCandidates()

        viewModel.uiState.test {
            val initialStateWithData = awaitItem()
            assertEquals(mockCandidateList, initialStateWithData.candidates)
            assertEquals("", initialStateWithData.searchQuery)


            viewModel.setSearchQuery("Smith")

            val filteredState = awaitItem()
            assertEquals(listOf(candidate1, candidate3), filteredState.candidates)
            assertEquals("Smith", filteredState.searchQuery)
            Assert.assertFalse(filteredState.isLoading)

            cancelAndConsumeRemainingEvents()
        }
    }

    @Test
    fun `setSearchQuery should update uiState with all candidates when query is blank`() = runTest {
        setupViewModelWithCandidates()

        viewModel.setSearchQuery("Smith")
        advanceUntilIdle()

        assertEquals("Smith", viewModel.uiState.value.searchQuery)
        assertEquals(listOf(candidate1, candidate3), viewModel.uiState.value.candidates)

        viewModel.uiState.test {
            val stateWithSmithFilter = awaitItem()
            assertEquals("Smith", stateWithSmithFilter.searchQuery)
            assertEquals(listOf(candidate1, candidate3), stateWithSmithFilter.candidates)

            viewModel.setSearchQuery(" ")

            val clearedFilterState = awaitItem()
            assertEquals(mockCandidateList, clearedFilterState.candidates)
            assertEquals("", clearedFilterState.searchQuery)

            cancelAndConsumeRemainingEvents()
        }
    }

    @Test
    fun `setSearchQuery should be case insensitive`() = runTest {
        setupViewModelWithCandidates()

        viewModel.uiState.test {
            awaitItem()

            viewModel.setSearchQuery("alice")

            val filteredState = awaitItem()
            assertEquals(listOf(candidate1), filteredState.candidates)
            assertEquals("alice", filteredState.searchQuery)

            cancelAndConsumeRemainingEvents()
        }
    }

    @Test
    fun `setSearchQuery should return empty list when no candidates match query`() = runTest {
        setupViewModelWithCandidates()

        viewModel.uiState.test {
            awaitItem()
            viewModel.setSearchQuery("NonExistentName")

            val noMatchState = awaitItem()
            assertTrue(noMatchState.candidates.isEmpty())
            assertEquals("NonExistentName", noMatchState.searchQuery)

            cancelAndConsumeRemainingEvents()
        }
    }

    @Test
    fun `setSearchQuery should trim whitespace from query`() = runTest {
        setupViewModelWithCandidates()

        viewModel.uiState.test {
            awaitItem()

            viewModel.setSearchQuery("  Bob  ")

            val trimmedQueryState = awaitItem()
            assertEquals(listOf(candidate2), trimmedQueryState.candidates)
            assertEquals("Bob", trimmedQueryState.searchQuery)

            cancelAndConsumeRemainingEvents()
        }
    }

    @Test
    fun `fetchAllCandidatesIfNeeded should call fetchAllCandidates when source is empty`() = runTest {
        whenever(getAllCandidatesUseCase.execute()).thenReturn(flowOf(DataResult.Success(mockCandidateList)))

        viewModel.uiState.test {
            awaitItem() // Initial state

            viewModel.fetchAllCandidatesIfNeeded()

            assertTrue(awaitItem().isLoading) // Loading

            val intermediateSuccess = awaitItem()
            Assert.assertFalse(intermediateSuccess.isLoading)
            assertTrue(intermediateSuccess.candidates.isEmpty())

            val finalSuccess = awaitItem()
            Assert.assertFalse(finalSuccess.isLoading)
            assertEquals(mockCandidateList, finalSuccess.candidates)

            cancelAndConsumeRemainingEvents()
        }
        verify(getAllCandidatesUseCase, times(1)).execute()
    }

    @Test
    fun `fetchAllCandidatesIfNeeded should NOT call fetchAllCandidates when source is NOT empty`() = runTest {
        whenever(getAllCandidatesUseCase.execute()).thenReturn(flowOf(DataResult.Success(mockCandidateList)))
        viewModel.fetchAllCandidates()
        viewModel.uiState.test {
            awaitItem() // initial
            awaitItem() // loading
            awaitItem() // intermediate
            awaitItem() // final
            cancelAndConsumeRemainingEvents()
        }
        advanceUntilIdle()

        viewModel.fetchAllCandidatesIfNeeded()
        advanceUntilIdle()

        verify(getAllCandidatesUseCase, times(1)).execute()

        viewModel.uiState.test {
            val currentState = awaitItem()
            Assert.assertFalse(currentState.isLoading) // Ne devrait pas être en chargement
            assertEquals(mockCandidateList, currentState.candidates) // Devrait toujours avoir les données initiales
            cancelAndConsumeRemainingEvents()
        }
    }

    @Test
    fun `clearErrorMessage should set errorMessage to null`() = runTest {
        val errorMessage = "Test Error Message"
        whenever(getAllCandidatesUseCase.execute()).thenReturn(flowOf(DataResult.Error(Exception(errorMessage))))
        viewModel.fetchAllCandidates()
        viewModel.uiState.test { // Consommer les états jusqu'à l'erreur
            awaitItem() // initial
            awaitItem() // loading
            val errorState = awaitItem() // error
            Assert.assertNotNull(errorState.errorMessage)
            cancelAndConsumeRemainingEvents()
        }
        advanceUntilIdle()

        // 2. Act
        viewModel.clearErrorMessage()

        // 3. Assert
        viewModel.uiState.test {
            val stateAfterClear = awaitItem()
            assertNull(stateAfterClear.errorMessage)
            Assert.assertFalse(stateAfterClear.isLoading)
            assertTrue(stateAfterClear.candidates.isEmpty())
            cancelAndConsumeRemainingEvents()
        }
    }
}

@ExperimentalCoroutinesApi
class MainDispatcherRule(
    private val testDispatcher: TestDispatcher = StandardTestDispatcher()
) : TestWatcher() {
    // Correct signature for starting
    override fun starting(description: Description) {
        Dispatchers.setMain(testDispatcher)
    }

    // Correct signature for finished
    override fun finished(description: Description) {
        Dispatchers.resetMain()
    }
}
