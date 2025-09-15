package com.quizocr.vitesse.domain.model

import com.google.gson.annotations.SerializedName

data class ExchangeRateResponse(
    @SerializedName("date")
    val date: String?,

    @SerializedName("currency_rates")
    val currencyRates: Map<String, Double>?,

)
