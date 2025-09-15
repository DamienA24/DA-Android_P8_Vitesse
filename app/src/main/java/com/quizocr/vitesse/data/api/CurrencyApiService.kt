package com.quizocr.vitesse.data.api

import com.quizocr.vitesse.data.api.model.ExchangeRateResponse
import retrofit2.Response
import retrofit2.http.GET
import retrofit2.http.Path

interface CurrencyApiService {
    @GET("{currency}.json")
    suspend fun getCurrencyRates(
        @Path("currency") currencyCode: String
    ): Response<ExchangeRateResponse>
}
