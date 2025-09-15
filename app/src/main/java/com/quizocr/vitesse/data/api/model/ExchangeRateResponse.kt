package com.quizocr.vitesse.data.api.model

import com.google.gson.annotations.SerializedName

data class ExchangeRateResponse(
    @SerializedName("date")
    val date: String?,

    @SerializedName("eur")
    val eur: Map<String, Double>?
)


