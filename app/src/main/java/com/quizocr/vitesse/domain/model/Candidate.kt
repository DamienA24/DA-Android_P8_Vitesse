package com.quizocr.vitesse.domain.model

import java.time.LocalDate

data class Candidate(
    val id: Int,
    val photoUri: String,
    val firstName: String,
    val lastName: String,
    val phoneNumber: String,
    val email: String,
    val dateOfBirth: LocalDate,
    val salaryEuros: Double,
    val notes: String,
    val isFavorite: Boolean,
    val createdAt: Long,
    val updatedAt: Long
)

