package com.quizocr.vitesse.domain.model

data class ConversionRate(
    val fromCurrency: String,
    val toCurrency: String,
    val rate: Double
)
