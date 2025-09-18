package com.quizocr.vitesse.useCaseTest

import org.junit.Assert
import app.cash.turbine.test
import com.quizocr.vitesse.data.repository.CurrencyConversionRepository
import com.quizocr.vitesse.data.repository.DataResult
import com.quizocr.vitesse.domain.model.ConversionRate
import com.quizocr.vitesse.domain.usecase.GetCurrencyConversionRateUseCase
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import org.mockito.kotlin.mock
import org.mockito.kotlin.whenever

@ExperimentalCoroutinesApi
class GetCurrencyConversionRateUseCaseTest {

    private lateinit var mockCurrencyConversionRepository: CurrencyConversionRepository
    private lateinit var getCurrencyConversionRateUseCase: GetCurrencyConversionRateUseCase


    private val fromCurrencyCode = "EUR"
    private val toCurrencyCode = "GBP"
    private val mockConversionRate = ConversionRate(
        fromCurrency = fromCurrencyCode,
        toCurrency = toCurrencyCode,
        rate = 0.85
    )

    @Before
    fun setUp() {
        mockCurrencyConversionRepository = mock()
        getCurrencyConversionRateUseCase = GetCurrencyConversionRateUseCase(mockCurrencyConversionRepository)
    }

    @Test
    fun `invoke should call repository getExchangeRate and return success result`() = runTest {
        // Arrange:
        val successFlow = flowOf(DataResult.Success(mockConversionRate))
        whenever(mockCurrencyConversionRepository.getExchangeRate(fromCurrencyCode, toCurrencyCode))
            .thenReturn(successFlow)

        // Act:
        val resultFlow = getCurrencyConversionRateUseCase(fromCurrencyCode, toCurrencyCode)

        // Assert:
        resultFlow.test {
            val result = awaitItem()
            assertTrue("Result should be Success", result is DataResult.Success)
            Assert.assertEquals(mockConversionRate, (result as DataResult.Success).data)
            awaitComplete() // S'assurer que le Flow se termine
        }
    }

    @Test
    fun `invoke should call repository getExchangeRate and return error result when repository fails`() = runTest {
        // Arrange:
        val errorMessage = "API error or mapping failed"
        val exception = Exception(errorMessage)
        val errorFlow = flowOf(DataResult.Error(exception) as DataResult<ConversionRate>)
        whenever(mockCurrencyConversionRepository.getExchangeRate(fromCurrencyCode, toCurrencyCode))
            .thenReturn(errorFlow)

        // Act:
        val resultFlow = getCurrencyConversionRateUseCase(fromCurrencyCode, toCurrencyCode)

        // Assert:
        resultFlow.test {
            val result = awaitItem()
            assertTrue("Result should be Error", result is DataResult.Error)
            Assert.assertEquals(errorMessage, (result as DataResult.Error).exception.message)
            awaitComplete()

        }
    }
}


