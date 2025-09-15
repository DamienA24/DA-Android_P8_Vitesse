package com.quizocr.vitesse.data.api.remote

import com.quizocr.vitesse.data.api.CurrencyApiService
import com.quizocr.vitesse.data.api.model.ExchangeRateResponse
import com.quizocr.vitesse.data.repository.DataResult
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import retrofit2.Response
import java.io.IOException
import javax.inject.Inject

class CurrencyRemoteDataSource @Inject constructor(
    private val apiService: CurrencyApiService
) {
    suspend fun fetchCurrencyExchangeRates(currency: String): DataResult<ExchangeRateResponse> {
        return try {
            withContext(Dispatchers.IO) {
                val response: Response<ExchangeRateResponse> = apiService.getCurrencyRates(currency
                )
                if (response.isSuccessful) {
                    val body = response.body()
                    if (body != null && body.eur != null) {
                        DataResult.Success(body)
                    } else {
                        DataResult.Error(Exception("API response body is null or EUR rates are missing"))
                    }
                } else {
                    val errorBody = response.errorBody()?.string() ?: "Unknown HTTP error"
                    DataResult.Error(Exception("HTTP Error: ${response.code()} - $errorBody"))
                }
            }
        } catch (e: IOException) {
            DataResult.Error(Exception("Network error: ${e.message}", e))
        } catch (e: Exception) {
            DataResult.Error(Exception("An unexpected error occurred: ${e.message}", e))
        }
    }
}
