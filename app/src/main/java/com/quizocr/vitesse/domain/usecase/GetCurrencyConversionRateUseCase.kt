package com.quizocr.vitesse.domain.usecase


import com.quizocr.vitesse.data.repository.DataResult
import com.quizocr.vitesse.domain.model.ConversionRate
import com.quizocr.vitesse.data.repository.CurrencyConversionRepository
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

class GetCurrencyConversionRateUseCase @Inject constructor(
    private val repository: CurrencyConversionRepository
) {
    operator fun invoke(fromCurrency: String, toCurrency: String): Flow<DataResult<ConversionRate>> {
        return repository.getExchangeRate(fromCurrency, toCurrency)
    }
}