package com.quizocr.vitesse.data.repository.service

import com.quizocr.vitesse.data.repository.DataResult
import com.quizocr.vitesse.domain.model.ConversionRate
import kotlinx.coroutines.flow.Flow

interface ExchangeRateApi {
    fun getExchangeRate(fromCurrency: String, toCurrency: String): Flow<DataResult<ConversionRate>>
}