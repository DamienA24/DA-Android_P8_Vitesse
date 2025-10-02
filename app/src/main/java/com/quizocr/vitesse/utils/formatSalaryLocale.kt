package com.quizocr.vitesse.utils

import android.util.Log
import androidx.core.net.ParseException
import java.text.DecimalFormatSymbols
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

fun parseFormattedSalaryString(salaryString: String?, currentLocale: Locale = Locale.getDefault()): Double? {
    if (salaryString.isNullOrBlank()) {
        Log.d("ParseFormattedSalary", "Input string is null or blank, returning null.")
        return null
    }

    Log.d("ParseFormattedSalary", "Attempting to parse '$salaryString' using info from locale '${currentLocale.toLanguageTag()}'")

    val symbols = DecimalFormatSymbols(currentLocale)
    val decimalSeparator = symbols.decimalSeparator
    val groupingSeparator = symbols.groupingSeparator

    var cleanedString = salaryString
    if (groupingSeparator.toString().isNotBlank()) {
        cleanedString = cleanedString.replace(groupingSeparator.toString(), "")
    }


    if (decimalSeparator != '.') {
        cleanedString = cleanedString.replace(decimalSeparator, '.')
    }

    return try {
        val doubleValue = cleanedString.toDoubleOrNull()
        if (doubleValue != null && doubleValue.isFinite()) {
            Log.d("ParseFormattedSalary", "Parsed: '$salaryString' -> LocaleDecSep='${decimalSeparator}', LocaleGrpSep='${groupingSeparator}' -> Cleaned='$cleanedString' -> $doubleValue")
            doubleValue
        } else {
            Log.w("ParseFormattedSalary", "Failed to parse or result not finite after cleaning: '$cleanedString' -> $doubleValue. Original: '$salaryString'")
            val fallbackCleaned = salaryString.replace(Regex("[^0-9.]"), "")
            fallbackCleaned.toDoubleOrNull()?.takeIf { it.isFinite() }
        }
    } catch (e: NumberFormatException) {
        Log.e("ParseFormattedSalary", "NumberFormatException for cleaned string '$cleanedString'. Original: '$salaryString'", e)
        null
    }
}