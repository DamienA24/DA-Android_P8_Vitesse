package com.quizocr.vitesse.utils

import android.os.Build
import androidx.annotation.RequiresApi
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import java.time.format.FormatStyle
import java.time.temporal.ChronoUnit


@RequiresApi(Build.VERSION_CODES.O)
fun formatDateShort(date: LocalDate): String {
    val dateFormatter = DateTimeFormatter.ofLocalizedDate(FormatStyle.SHORT)
    return date.format(dateFormatter)
}

@RequiresApi(Build.VERSION_CODES.O)
fun calculateAgeInYears(dateOfBirth: LocalDate): Long {
    val today = LocalDate.now()
    return ChronoUnit.YEARS.between(dateOfBirth, today)
}

@RequiresApi(Build.VERSION_CODES.O)
fun getFormattedBirthDateAndAge(dateOfBirth: LocalDate): Pair<String, Long> {
    val formattedDate = formatDateShort(dateOfBirth)
    val age = calculateAgeInYears(dateOfBirth)
    return Pair(formattedDate, age)
}