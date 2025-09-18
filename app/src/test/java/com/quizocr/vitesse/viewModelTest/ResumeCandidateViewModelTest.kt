package com.quizocr.vitesse.viewModelTest

import androidx.lifecycle.SavedStateHandle
import app.cash.turbine.test
import com.quizocr.vitesse.data.repository.DataResult
import com.quizocr.vitesse.domain.model.Candidate
import com.quizocr.vitesse.domain.model.ConversionRate
import com.quizocr.vitesse.domain.usecase.DeleteCandidateUseCase
import com.quizocr.vitesse.domain.usecase.GetCandidateById
import com.quizocr.vitesse.domain.usecase.GetCurrencyConversionRateUseCase
import com.quizocr.vitesse.domain.usecase.UpdateCandidateFavoriteStatusUseCase
import com.quizocr.vitesse.ui.resumeCandidate.ResumeCandidateViewModel
import com.quizocr.vitesse.utils.formatCurrencyInUk
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.runTest
import org.junit.Assert
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.mockito.kotlin.any
import org.mockito.kotlin.mock
import org.mockito.kotlin.verify
import org.mockito.kotlin.verifyNoInteractions
import org.mockito.kotlin.whenever
import java.time.LocalDate

@ExperimentalCoroutinesApi
class ResumeCandidateViewModelTest {

    @get:Rule
    val mainDispatcherRule = MainDispatcherRule()

    // Mocks pour les Use Cases
    private lateinit var getCandidateByIdUseCase: GetCandidateById
    private lateinit var getCurrencyConversionRateUseCase: GetCurrencyConversionRateUseCase
    private lateinit var updateCandidateFavoriteStatusUseCase: UpdateCandidateFavoriteStatusUseCase
    private lateinit var deleteCandidateUseCase: DeleteCandidateUseCase

    private lateinit var savedStateHandle: SavedStateHandle
    private lateinit var viewModel: ResumeCandidateViewModel

    // Données de test
    private val testCandidateId = 1
    private val testCandidate = Candidate(
        id = testCandidateId,
        firstName = "John",
        lastName = "Doe",
        phoneNumber = "123456789",
        email = "john.doe@example.com",
        dateOfBirth = LocalDate.of(1990, 1, 1),
        salaryEuros = 50000.0,
        notes = "Test notes",
        isFavorite = false,
        createdAt = System.currentTimeMillis(),
        updatedAt = System.currentTimeMillis()
    )
    private val testConversionRate = ConversionRate("EUR", "GBP", 0.85)

    @Before
    fun setUp() {
        // Initialiser les mocks
        getCandidateByIdUseCase = mock()
        getCurrencyConversionRateUseCase = mock()
        updateCandidateFavoriteStatusUseCase = mock()
        deleteCandidateUseCase = mock()
        // savedStateHandle et viewModel seront initialisés dans chaque test ou via une fonction d'aide
    }

    private fun initializeViewModel(candidateId: Int?) {
        savedStateHandle = SavedStateHandle().apply {
            candidateId?.let { set("candidateId", it) }
        }
        viewModel = ResumeCandidateViewModel(
            getCandidateByIdUseCase,
            getCurrencyConversionRateUseCase,
            updateCandidateFavoriteStatusUseCase,
            deleteCandidateUseCase,
            savedStateHandle
        )
    }

    @Test
    fun `initialization when valid candidateId loads candidate and converts salary`() = runTest {
        // Arrange
        whenever(getCandidateByIdUseCase.execute(testCandidateId)).thenReturn(
            flowOf(
                DataResult.Success(
                    testCandidate
                )
            )
        )
        whenever(
            getCurrencyConversionRateUseCase(
                "eur",
                "gbp"
            )
        ).thenReturn(flowOf(DataResult.Success(testConversionRate)))
        initializeViewModel(testCandidateId)

        // Assert
        viewModel.uiState.test {
            var emittedState = awaitItem()
            Assert.assertFalse("Initial state isLoading should be false", emittedState.isLoading)

            emittedState = awaitItem() //
            Assert.assertTrue("isLoading should be true after onStart", emittedState.isLoading)

            emittedState = awaitItem()
            println("emittedState: $emittedState")
            Assert.assertTrue(
                "isLoading should be true after candidate is loaded (before conversion finishes for this state check)",
                emittedState.isLoading && !emittedState.isConvertingCurrency
            )
            Assert.assertEquals(testCandidate, emittedState.candidate)

            val finalState = awaitItem()
            Assert.assertFalse(finalState.isLoading)
            Assert.assertFalse(finalState.isConvertingCurrency)
            Assert.assertEquals(testCandidate, finalState.candidate)
            Assert.assertNotNull(finalState.formattedSalaryPounds)
            val expectedSalaryPounds =
                formatCurrencyInUk(testCandidate.salaryEuros * testConversionRate.rate)
            Assert.assertEquals(expectedSalaryPounds, finalState.formattedSalaryPounds)

            cancelAndConsumeRemainingEvents()
        }
        verify(getCandidateByIdUseCase).execute(testCandidateId)
        verify(getCurrencyConversionRateUseCase).invoke("eur", "gbp")
    }

    @Test
    fun `initialization when invalid candidateId sets error state`() = runTest {
        initializeViewModel(-1) // ID invalide

        viewModel.uiState.test {
            val emittedState = awaitItem()
            Assert.assertFalse(emittedState.isLoading)
            Assert.assertEquals("Invalid Candidate ID.", emittedState.errorMessage)
            Assert.assertNull(emittedState.candidate)
            cancelAndConsumeRemainingEvents()
        }
        verifyNoInteractions(getCandidateByIdUseCase)
        verifyNoInteractions(getCurrencyConversionRateUseCase)
    }

    @Test
    fun `initialization when getCandidateById fails sets error state`() = runTest {
        val exception = Exception("DB Error")
        whenever(getCandidateByIdUseCase.execute(testCandidateId)).thenReturn(
            flowOf(
                DataResult.Error(
                    exception
                )
            )
        )
        initializeViewModel(testCandidateId)

        viewModel.uiState.test {
            awaitItem() // Initial
            awaitItem() // Loading
            val errorState = awaitItem()
            println("errorState: $errorState")
            Assert.assertFalse(errorState.isLoading)
            Assert.assertEquals("DB Error", errorState.errorMessage)
            Assert.assertNull(errorState.candidate)
            cancelAndConsumeRemainingEvents()
        }
        verify(getCandidateByIdUseCase).execute(testCandidateId)
        verifyNoInteractions(getCurrencyConversionRateUseCase)
    }

    @Test
    fun `initialization when currency conversion fails sets error message`() = runTest {
        val conversionException = Exception("API Error")
        whenever(getCandidateByIdUseCase.execute(testCandidateId)).thenReturn(
            flowOf(
                DataResult.Success(
                    testCandidate
                )
            )
        )
        whenever(getCurrencyConversionRateUseCase("eur", "gbp")).thenReturn(
            flowOf(
                DataResult.Error(
                    conversionException
                )
            )
        )
        initializeViewModel(testCandidateId)

        viewModel.uiState.test {
            awaitItem() // Initial isLoading=true
            awaitItem() // Candidate loaded,
            awaitItem()
            val finalState = awaitItem()

            Assert.assertFalse(
                "isLoading should be false after conversion attempt",
                finalState.isLoading
            )
            Assert.assertFalse(
                "isConvertingCurrency should be false after conversion failure",
                finalState.isConvertingCurrency
            )
            Assert.assertEquals(testCandidate, finalState.candidate)
            Assert.assertEquals("API Error", finalState.errorMessage)
            cancelAndConsumeRemainingEvents()
        }
    }


    // --- Tests de Statut Favori ---
    @Test
    fun `toggleFavoriteStatus when candidate exists and API call succeeds updates UI state`() =
        runTest {
            val initialCandidateFav = testCandidate.copy(isFavorite = false)
            whenever(getCandidateByIdUseCase.execute(testCandidateId)).thenReturn(
                flowOf(
                    DataResult.Success(
                        initialCandidateFav
                    )
                )
            )
            whenever(
                getCurrencyConversionRateUseCase(
                    any(),
                    any()
                )
            ).thenReturn(flowOf(DataResult.Success(testConversionRate)))
            initializeViewModel(testCandidateId)

            // Attendre que le chargement initial et la conversion soient terminés
            viewModel.uiState.first { it.candidate != null && !it.isLoading && !it.isConvertingCurrency && it.formattedSalaryPounds != null }

            whenever(
                updateCandidateFavoriteStatusUseCase.execute(
                    testCandidateId,
                    true
                )
            ).thenReturn(DataResult.Success(Unit))

            viewModel.toggleFavoriteStatus()

            viewModel.uiState.test {
                // L'état actuel après le chargement initial
                val stateBeforeToggle = awaitItem()
                // La mise à jour de isFavorite par toggleFavoriteStatus est synchrone dans le _uiState.update
                val stateAfterToggle = awaitItem()

                Assert.assertEquals(true, stateAfterToggle.candidate?.isFavorite)
                Assert.assertNull(stateAfterToggle.errorMessage)
                cancelAndConsumeRemainingEvents()
            }
            verify(updateCandidateFavoriteStatusUseCase).execute(testCandidateId, true)
        }

    @Test
    fun `toggleFavoriteStatus when API call fails sets error message`() = runTest {
        val initialCandidateFav = testCandidate.copy(isFavorite = false)
        whenever(getCandidateByIdUseCase.execute(testCandidateId)).thenReturn(
            flowOf(
                DataResult.Success(
                    initialCandidateFav
                )
            )
        )
        whenever(
            getCurrencyConversionRateUseCase(
                any(),
                any()
            )
        ).thenReturn(flowOf(DataResult.Success(testConversionRate)))
        initializeViewModel(testCandidateId)

        viewModel.uiState.first { it.candidate != null && !it.isLoading && !it.isConvertingCurrency && it.formattedSalaryPounds != null }

        val favException = Exception("Favorite Update Error")
        whenever(updateCandidateFavoriteStatusUseCase.execute(testCandidateId, true)).thenReturn(
            DataResult.Error(favException)
        )

        viewModel.toggleFavoriteStatus()

        viewModel.uiState.test {
            awaitItem() // État avant l'erreur (après l'initialisation complète)
            val errorState = awaitItem() // État après l'échec de la mise à jour du favori

            Assert.assertEquals("Favorite Update Error", errorState.errorMessage)
            Assert.assertEquals(
                false,
                errorState.candidate?.isFavorite
            ) // Le favori n'a pas dû changer
            cancelAndConsumeRemainingEvents()
        }
    }


    // --- Tests de Suppression de Candidat ---
    @Test
    fun `deleteCandidate when candidate exists and API call succeeds updates UI for navigation`() =
        runTest {
            whenever(getCandidateByIdUseCase.execute(testCandidateId)).thenReturn(
                flowOf(
                    DataResult.Success(
                        testCandidate
                    )
                )
            )
            whenever(
                getCurrencyConversionRateUseCase(
                    any(),
                    any()
                )
            ).thenReturn(flowOf(DataResult.Success(testConversionRate)))
            initializeViewModel(testCandidateId)
            viewModel.uiState.first { it.candidate != null && !it.isLoading && !it.isConvertingCurrency && it.formattedSalaryPounds != null }

            whenever(deleteCandidateUseCase.execute(testCandidate)).thenReturn(
                DataResult.Success(
                    Unit
                )
            )

            viewModel.deleteCandidate()

            viewModel.uiState.test {
                awaitItem() // État avant la suppression
                val deletingState = awaitItem() // isDeleting = true
                Assert.assertTrue(deletingState.isDeleting)
                Assert.assertEquals(
                    testCandidate,
                    deletingState.candidate
                ) // Le candidat est toujours là pendant la suppression

                val finalState = awaitItem() // Après suppression
                Assert.assertFalse(finalState.isDeleting)
                Assert.assertNull(finalState.candidate) // Le candidat est null
                Assert.assertTrue(finalState.navigateBackAfterDeletion)
                cancelAndConsumeRemainingEvents()
            }
            verify(deleteCandidateUseCase).execute(testCandidate)
        }

    @Test
    fun `deleteCandidate when API call fails sets error message and isDeleting to false`() =
        runTest {
            whenever(getCandidateByIdUseCase.execute(testCandidateId)).thenReturn(
                flowOf(
                    DataResult.Success(
                        testCandidate
                    )
                )
            )
            whenever(
                getCurrencyConversionRateUseCase(
                    any(),
                    any()
                )
            ).thenReturn(flowOf(DataResult.Success(testConversionRate)))
            initializeViewModel(testCandidateId)
            viewModel.uiState.first { it.candidate != null && !it.isLoading && !it.isConvertingCurrency && it.formattedSalaryPounds != null }

            val deleteException = Exception("Deletion Error")
            whenever(deleteCandidateUseCase.execute(testCandidate)).thenReturn(
                DataResult.Error(
                    deleteException
                )
            )

            viewModel.deleteCandidate()

            viewModel.uiState.test {
                awaitItem() // initial
                val deletingState = awaitItem() // isDeleting = true
                Assert.assertTrue(deletingState.isDeleting)

                awaitItem() // isDeleting = false
                val errorState = awaitItem() // error state
                Assert.assertFalse(errorState.isDeleting)
                Assert.assertEquals(testCandidate, errorState.candidate)
                Assert.assertEquals("Deletion Error", errorState.errorMessage)
                Assert.assertFalse(errorState.navigateBackAfterDeletion)
                cancelAndConsumeRemainingEvents()
            }
        }

    // --- Test de Navigation ---
    @Test
    fun `onNavigationDone then sets navigateBackAfterDeletion to false`() = runTest {
        initializeViewModel(null)


        viewModel.uiState.test {
            val state = awaitItem()
            Assert.assertFalse(state.navigateBackAfterDeletion)
            cancelAndConsumeRemainingEvents()
        }

        viewModel.onNavigationDone()

        viewModel.uiState.test {
            val state = awaitItem()
            Assert.assertFalse(state.navigateBackAfterDeletion)
            cancelAndConsumeRemainingEvents()
        }
    }
}