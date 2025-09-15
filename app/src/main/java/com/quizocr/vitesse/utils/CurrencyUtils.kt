package com.quizocr.vitesse.utils

import android.icu.text.NumberFormat
import java.util.Locale

fun formatCurrencyInUk(amount: Double): String {
    return try {
        NumberFormat.getCurrencyInstance(Locale.UK).format(amount)
    } catch (e: Exception) {
        amount.toString() // Fallback
    }
}