package com.quizocr.vitesse.data.repository

import android.util.Log
import com.quizocr.vitesse.data.api.model.ExchangeRateResponse
import com.quizocr.vitesse.data.api.remote.CurrencyRemoteDataSource
import com.quizocr.vitesse.data.repository.service.ExchangeRateApi
import com.quizocr.vitesse.domain.model.ConversionRate
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import javax.inject.Inject

class CurrencyConversionRepository @Inject constructor(
    private val remoteDataSource: CurrencyRemoteDataSource
) : ExchangeRateApi {

    override fun getExchangeRate(fromCurrency: String, toCurrency: String): Flow<DataResult<ConversionRate>> = flow {

        if (fromCurrency.isBlank()) {
            emit(DataResult.Error(Exception("From currency cannot be blank.")))
            return@flow
        }

        when (val apiResult = remoteDataSource.fetchCurrencyExchangeRates(fromCurrency)) {
            is DataResult.Success -> {
                val domainModel = mapApiResponseToDomain(apiResult.data, fromCurrency, toCurrency)
                if (domainModel != null) {
                    Log.d("CurrencyConversionRepo", "Domain Model: $domainModel")
                    emit(DataResult.Success(domainModel))
                } else {
                    emit(DataResult.Error(Exception("Rate for $fromCurrency not found in API response or mapping failed.")))
                }
            }
            is DataResult.Error -> {
                emit(DataResult.Error(apiResult.exception))
            }
        }
    }

    private fun mapApiResponseToDomain(response: ExchangeRateResponse, fromCurrency: String, targetCurrencyCode: String): ConversionRate? {

        val rate = response.eur?.get(targetCurrencyCode.lowercase())
        return if (rate != null) {
            ConversionRate(
                fromCurrency = fromCurrency,
                toCurrency = targetCurrencyCode,
                rate = rate
            )
        } else {
            null
        }
    }
}
