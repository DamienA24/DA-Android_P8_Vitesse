package com.quizocr.vitesse.viewModelTest

import androidx.lifecycle.SavedStateHandle
import app.cash.turbine.test
import com.quizocr.vitesse.data.repository.DataResult
import com.quizocr.vitesse.domain.model.Candidate
import com.quizocr.vitesse.domain.usecase.AddNewCandidateUseCase
import com.quizocr.vitesse.domain.usecase.GetCandidateById
import com.quizocr.vitesse.domain.usecase.UpdateCandidateUseCase
import com.quizocr.vitesse.ui.addEditCandidate.AddEditCandidateViewModel
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.first // For consuming initial states before assertions on later ones
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.mockito.kotlin.any
import org.mockito.kotlin.argumentCaptor
import org.mockito.kotlin.eq
import org.mockito.kotlin.mock
import org.mockito.kotlin.never
import org.mockito.kotlin.verify
import org.mockito.kotlin.whenever
import java.time.LocalDate

@ExperimentalCoroutinesApi
class AddEditCandidateViewModelTest {

    @get:Rule
    val mainDispatcherRule = MainDispatcherRule()

    private lateinit var getCandidateByIdUseCase: GetCandidateById
    private lateinit var addNewCandidateUseCase: AddNewCandidateUseCase
    private lateinit var updateCandidateUseCase: UpdateCandidateUseCase
    private lateinit var savedStateHandle: SavedStateHandle
    private lateinit var viewModel: AddEditCandidateViewModel

    private val testExistingCandidateId = 1
    private val testNewCandidateId = -1
    private val testScreenTitle = "Test Screen Title"

    private val originalCandidate = Candidate(
        id = testExistingCandidateId,
        firstName = "John",
        lastName = "Doe",
        phoneNumber = "123456789",
        email = "john.doe@example.com",
        dateOfBirth = LocalDate.of(1990, 1, 1),
        salaryEuros = 50000.0,
        notes = "Original notes",
        isFavorite = false,
        createdAt = 1000L,
        updatedAt = 1000L
    )

    private val newCandidateData = Candidate( // Data used for creating a new candidate
        id = 0, // Should be 0 when passing to AddNewCandidateUseCase
        firstName = "Jane",
        lastName = "Smith",
        phoneNumber = "987654321",
        email = "jane.smith@example.com",
        dateOfBirth = LocalDate.of(1992, 2, 2),
        salaryEuros = 60000.0,
        notes = "New candidate notes",
        photoUri = "uri://new_photo",
        isFavorite = false, // Default for new
        createdAt = 0L, // Will be set by ViewModel before saving
        updatedAt = 0L  // Will be set by ViewModel before saving
    )


    @Before
    fun setUp() {
        getCandidateByIdUseCase = mock()
        addNewCandidateUseCase = mock()
        updateCandidateUseCase = mock()
    }

    private fun initializeViewModel(candidateId: Int, screenTitle: String?) {
        savedStateHandle = SavedStateHandle().apply {
            set("candidateId", candidateId)
            screenTitle?.let { set("screenTitle", it) }
        }
        viewModel = AddEditCandidateViewModel(
            getCandidateByIdUseCase,
            addNewCandidateUseCase,
            updateCandidateUseCase,
            savedStateHandle
        )
    }

    // --- Initialization Tests ---

    @Test
    fun `initialization with new candidate ID (-1) sets correct default UI state`() = runTest {
        initializeViewModel(testNewCandidateId, testScreenTitle)
        viewModel.uiState.test {
            val initialState = awaitItem()
            assertEquals(testNewCandidateId, viewModel.candidateId)
            assertEquals(testScreenTitle, initialState.screenTitle)
            assertNull("Candidate should be null for new candidate ID", initialState.candidate)
            assertFalse("isLoadingCandidateData should be false", initialState.isLoadingCandidateData)
            verify(getCandidateByIdUseCase, never()).execute(any())
            cancelAndConsumeRemainingEvents()
        }
    }

    @Test
    fun `initialization with existing candidate ID loads candidate successfully`() = runTest {
        whenever(getCandidateByIdUseCase.execute(testExistingCandidateId))
            .thenReturn(flowOf(DataResult.Success(originalCandidate)))
        initializeViewModel(testExistingCandidateId, testScreenTitle)

        viewModel.uiState.test {
            var emittedState = awaitItem() // Initial or loading
            if (!emittedState.isLoadingCandidateData) emittedState = awaitItem() // Ensure we see loading state

            assertTrue("isLoadingCandidateData should be true while loading", emittedState.isLoadingCandidateData)

            val successState = awaitItem() // State after successful load
            assertEquals(testExistingCandidateId, viewModel.candidateId)
            assertEquals(testScreenTitle, successState.screenTitle)
            assertEquals("Loaded candidate should match originalCandidate", originalCandidate, successState.candidate)
            assertFalse("isLoadingCandidateData should be false after loading", successState.isLoadingCandidateData)
            assertNull("errorMessage should be null on success", successState.errorMessage)
            cancelAndConsumeRemainingEvents()
        }
        verify(getCandidateByIdUseCase).execute(testExistingCandidateId)
    }

    @Test
    fun `initialization with existing candidate ID fails to load candidate`() = runTest {
        val errorMessage = "Database error"
        whenever(getCandidateByIdUseCase.execute(testExistingCandidateId))
            .thenReturn(flowOf(DataResult.Error(Exception(errorMessage))))
        initializeViewModel(testExistingCandidateId, testScreenTitle)

        viewModel.uiState.test {
            var emittedState = awaitItem()
            if (!emittedState.isLoadingCandidateData) emittedState = awaitItem()

            assertTrue("isLoadingCandidateData should be true", emittedState.isLoadingCandidateData)

            val errorState = awaitItem()
            assertEquals(testExistingCandidateId, viewModel.candidateId)
            assertEquals(testScreenTitle, errorState.screenTitle)
            assertNull("Candidate should be null on error", errorState.candidate)
            assertFalse("isLoadingCandidateData should be false after error", errorState.isLoadingCandidateData)
            assertEquals("Error message should be set", errorMessage, errorState.errorMessage)
            cancelAndConsumeRemainingEvents()
        }
        verify(getCandidateByIdUseCase).execute(testExistingCandidateId)
    }

    // --- Save (New Candidate) Tests ---

    @Test
    fun `saveCandidate for new candidate calls AddNewCandidateUseCase and updates UI state on success`() = runTest {
        initializeViewModel(testNewCandidateId, testScreenTitle)
        whenever(addNewCandidateUseCase.execute(any()))
            .thenReturn(DataResult.Success(Unit))

        viewModel.saveCandidate(
            firstName = newCandidateData.firstName, lastName = newCandidateData.lastName,
            phoneNumber = newCandidateData.phoneNumber, email = newCandidateData.email,
            dateOfBirth = newCandidateData.dateOfBirth, salary = newCandidateData.salaryEuros,
            notes = newCandidateData.notes, photoUri = newCandidateData.photoUri
        )

        viewModel.uiState.test {
            awaitItem() // Initial state

            var savingState = awaitItem() // isSaving = true
            assertTrue("isSaving should be true during save operation", savingState.isSaving)
            assertNull("Candidate in UI state should still be null during new save", savingState.candidate)


            val successState = awaitItem() // isSaving = false, saveSuccess = true
            assertFalse("isSaving should be false after save", successState.isSaving)
            assertTrue("saveSuccess should be true on success", successState.saveSuccess)
            assertNull("errorMessage should be null on success", successState.errorMessage)
            /*
            * For a new candidate, the ViewModel does NOT update the _uiState.candidate with the newly created one.
            * It remains null or as it was. The saveSuccess flag is used for navigation.
            */
            assertNull("Candidate in UI state should remain null after saving a new one", successState.candidate)

            cancelAndConsumeRemainingEvents()
        }
        val candidateCaptor = argumentCaptor<Candidate>()
        verify(addNewCandidateUseCase).execute(candidateCaptor.capture())
        assertEquals(0, candidateCaptor.firstValue.id) // New candidate ID should be 0
        assertEquals(newCandidateData.firstName, candidateCaptor.firstValue.firstName)
        verify(updateCandidateUseCase, never()).execute(any())
    }

    @Test
    fun `saveCandidate for new candidate calls AddNewCandidateUseCase and updates UI state on failure`() = runTest {
        initializeViewModel(testNewCandidateId, testScreenTitle)
        val errorMessage = "Network error"
        whenever(addNewCandidateUseCase.execute(any()))
            .thenReturn(DataResult.Error(Exception(errorMessage)))

        viewModel.saveCandidate(
            firstName = newCandidateData.firstName, lastName = newCandidateData.lastName,
            phoneNumber = newCandidateData.phoneNumber, email = newCandidateData.email,
            dateOfBirth = newCandidateData.dateOfBirth, salary = newCandidateData.salaryEuros,
            notes = newCandidateData.notes, photoUri = newCandidateData.photoUri
        )

        viewModel.uiState.test {
            awaitItem() // Initial
            val savingState = awaitItem() // isSaving = true
            assertTrue(savingState.isSaving)

            val errorState = awaitItem() // isSaving = false, errorMessage set
            assertFalse(errorState.isSaving)
            assertFalse(errorState.saveSuccess)
            assertEquals(errorMessage, errorState.errorMessage)
            assertNull("Candidate in UI state should still be null", errorState.candidate)
            cancelAndConsumeRemainingEvents()
        }
        verify(addNewCandidateUseCase).execute(any())
    }

    // --- Update (Existing Candidate) Tests ---

    @Test
    fun `saveCandidate for existing candidate calls UpdateCandidateUseCase and updates UI state on success`() = runTest {
        whenever(getCandidateByIdUseCase.execute(testExistingCandidateId))
            .thenReturn(flowOf(DataResult.Success(originalCandidate)))
        initializeViewModel(testExistingCandidateId, testScreenTitle)
        viewModel.uiState.first { it.candidate == originalCandidate } // Ensure candidate is loaded

        val updatedNotes = "Updated notes by test"
        val salaryToUpdate = 55000.0
        val delta = 0.001

        whenever(updateCandidateUseCase.execute(any()))
            .thenReturn(DataResult.Success(Unit))

        viewModel.saveCandidate(
            firstName = originalCandidate.firstName, lastName = originalCandidate.lastName,
            phoneNumber = originalCandidate.phoneNumber, email = originalCandidate.email,
            dateOfBirth = originalCandidate.dateOfBirth, salary = salaryToUpdate, // Changed
            notes = updatedNotes, // Changed
            photoUri = originalCandidate.photoUri
        )

        viewModel.uiState.test {
            awaitItem()
            val currentState = awaitItem()
            assertTrue("isSaving should be true during update", currentState.isSaving)
            // The candidate in state here should still be the original one before the copy inside saveCandidate happens
            assertEquals(originalCandidate, currentState.candidate)


            val successState = awaitItem() // isSaving = false, saveSuccess = true
            assertFalse("isSaving should be false after update", successState.isSaving)
            assertTrue("saveSuccess should be true on update success", successState.saveSuccess)
            assertNull("errorMessage should be null on update success", successState.errorMessage)

            /*
            * For an existing candidate, the ViewModel DOES update the _uiState.candidate
            * with the version of the candidate that was prepared for the update.
            */
            assertNotNull("Candidate in state should not be null", successState.candidate)
            assertEquals("Updated notes should be reflected", updatedNotes, successState.candidate?.notes)
            assertEquals("Updated salary should be reflected", salaryToUpdate, successState.candidate?.salaryEuros ?: 0.0, delta)
            assertEquals("ID should remain the same", testExistingCandidateId, successState.candidate?.id)
            // Check that updatedAt was likely changed (greater than original)
            assertTrue("updatedAt should be modified", (successState.candidate?.updatedAt ?: 0L) > originalCandidate.updatedAt)


            cancelAndConsumeRemainingEvents()
        }

        val candidateCaptor = argumentCaptor<Candidate>()
        verify(updateCandidateUseCase).execute(candidateCaptor.capture())
        assertEquals(testExistingCandidateId, candidateCaptor.firstValue.id)
        assertEquals(updatedNotes, candidateCaptor.firstValue.notes)
        assertEquals(salaryToUpdate, candidateCaptor.firstValue.salaryEuros, delta)
        verify(addNewCandidateUseCase, never()).execute(any())
    }

    @Test
    fun `saveCandidate for existing candidate but _uiState candidate is null, exits early and does not call updateUseCase`() = runTest {
        // Initialize for an existing ID, but DO NOT mock getCandidateByIdUseCase to make it return success.
        // This means _uiState.value.candidate will be null when saveCandidate is called.
        whenever(getCandidateByIdUseCase.execute(testExistingCandidateId))
            .thenReturn(flowOf(DataResult.Error(Exception("Simulated load error")))) // Or just never return success
        initializeViewModel(testExistingCandidateId, testScreenTitle)
        viewModel.uiState.first { it.errorMessage != null } // Ensure initial loading attempt has finished (e.g. error state)

        // At this point, _uiState.value.candidate is null in the ViewModel

        viewModel.saveCandidate( // Attempt to save
            firstName = "AttemptFirstName", lastName = "AttemptLastName",
            phoneNumber = "000", email = "attempt@example.com",
            dateOfBirth = LocalDate.now(), salary = 100.0,
            notes = "AttemptNotes", photoUri = null
        )

        viewModel.uiState.test {
            val stateBeforeSaveAttempt = awaitItem() // State after failed load
            assertNull(stateBeforeSaveAttempt.candidate)

            // Since the internal candidate is null, saveCandidate should return@launch.
            // No new state for "isSaving=true" should be emitted persistently.
            // We might see a brief "isSaving=true" if the test is very fast, then it reverts or the coroutine ends.
            // The safest is to ensure no SUCCESSFUL save state is reached and use cases are not called.

            // Let's ensure no further significant state changes occur due to the save attempt
            expectNoEvents() // Or check that the state remains largely unchanged.

            assertFalse("isSaving should remain false or revert quickly", viewModel.uiState.value.isSaving)
            assertFalse("saveSuccess should remain false", viewModel.uiState.value.saveSuccess)
        }

        verify(updateCandidateUseCase, never()).execute(any())
        verify(addNewCandidateUseCase, never()).execute(any())
    }


// Dans AddEditCandidateViewModelTest.kt

    @Test
    fun `saveCandidate for existing candidate calls UpdateCandidateUseCase and on failure UI state has error and original candidate data`() = runTest {
        whenever(getCandidateByIdUseCase.execute(testExistingCandidateId))
            .thenReturn(flowOf(DataResult.Success(originalCandidate)))
        initializeViewModel(testExistingCandidateId, testScreenTitle)
        viewModel.uiState.first { it.candidate == originalCandidate } // Ensure candidate is loaded

        val errorMessage = "Update database error"
        val attemptedNotes = "Attempted update notes" // These notes will be sent to the use case

        // Mock failed update
        whenever(updateCandidateUseCase.execute(any()))
            .thenReturn(DataResult.Error(Exception(errorMessage)))

        // WHEN: saveCandidate is called with new data, but it fails
        viewModel.saveCandidate(
            firstName = originalCandidate.firstName, lastName = originalCandidate.lastName,
            phoneNumber = originalCandidate.phoneNumber, email = originalCandidate.email,
            dateOfBirth = originalCandidate.dateOfBirth, salary = originalCandidate.salaryEuros,
            notes = attemptedNotes, // This is the data that will be attempted
            photoUri = originalCandidate.photoUri
        )

        viewModel.uiState.test {
            var currentState = awaitItem() // State after candidate loaded (originalCandidate)
            assertEquals("Initial candidate should be the original", originalCandidate, currentState.candidate)

            currentState = awaitItem() // State when isSaving = true
            assertTrue("isSaving should be true during update attempt", currentState.isSaving)
            assertEquals("Candidate in state should still be original during saving attempt", originalCandidate, currentState.candidate)

            val errorState = awaitItem() // State when isSaving = false, errorMessage is set
            assertFalse("isSaving should be false after failed update", errorState.isSaving)
            assertFalse("saveSuccess should be false on failed update", errorState.saveSuccess)
            assertEquals("Error message should match", errorMessage, errorState.errorMessage)

            /*
             * CURRENT ViewModel BEHAVIOR (Correct for failure):
             * On failure to update, the ViewModel does NOT persist the 'attempted' data in _uiState.candidate.
             * The _uiState.candidate should reflect the state from before the save attempt began,
             * or the state as it was when `isSaving` was set to true.
             */
            assertNotNull("Candidate in error state should not be null", errorState.candidate)
            assertEquals("On failure, candidate in UI state should be the original candidate", originalCandidate, errorState.candidate)
            assertEquals("On failure, notes in UI state should be original notes", originalCandidate.notes, errorState.candidate?.notes)

            cancelAndConsumeRemainingEvents()
        }

        // updateCandidateUseCase was called with the 'attempted' data
        val candidateCaptor = argumentCaptor<Candidate>()
        verify(updateCandidateUseCase).execute(candidateCaptor.capture())
        assertEquals("Captured candidate notes should be the attempted ones", attemptedNotes, candidateCaptor.firstValue.notes)
    }

    // --- UI Action Tests ---
    @Test
    fun `clearErrorMessage sets errorMessage to null in UI state`() = runTest {
        val initialErrorMessage = "An error occurred"
        whenever(getCandidateByIdUseCase.execute(testExistingCandidateId))
            .thenReturn(flowOf(DataResult.Error(Exception(initialErrorMessage))))
        initializeViewModel(testExistingCandidateId, testScreenTitle)
        viewModel.uiState.first { it.errorMessage == initialErrorMessage }

        viewModel.clearErrorMessage()

        viewModel.uiState.test {
            val finalState = awaitItem()
            assertNull("errorMessage should be null after clearErrorMessage", finalState.errorMessage)
            cancelAndConsumeRemainingEvents()
        }
    }
}
