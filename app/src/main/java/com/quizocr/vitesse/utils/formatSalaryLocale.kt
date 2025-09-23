package com.quizocr.vitesse.utils

import java.text.NumberFormat
import java.util.Locale

fun formatSalaryLocale(salary: Double): String  {
        val numberFormat = NumberFormat.getNumberInstance(Locale.getDefault())

        if (salary == salary.toInt().toDouble()) {
            numberFormat.maximumFractionDigits = 0
            numberFormat.minimumFractionDigits = 0
        } else {
            numberFormat.maximumFractionDigits = 2
            numberFormat.minimumFractionDigits = 2
        }
        return numberFormat.format(salary)
}