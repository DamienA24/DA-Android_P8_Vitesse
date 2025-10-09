package com.quizocr.vitesse.domain.model

import com.quizocr.vitesse.data.entity.CandidateEntity
import java.time.LocalDate

data class Candidate(
    val id: Int = 0,
    val photoUri: String? = null,
    val firstName: String,
    val lastName: String,
    val phoneNumber: String,
    val email: String,
    val dateOfBirth: LocalDate,
    val salaryEuros: Double = 0.0,
    val notes: String? = null,
    val isFavorite: Boolean = false,
    val createdAt: Long = System.currentTimeMillis(),
    val updatedAt: Long = System.currentTimeMillis()
) {
    companion object {
        fun fromEntity(entity: CandidateEntity): Candidate {
            return Candidate(
                id = entity.id,
                photoUri = entity.photoUri,
                firstName = entity.firstName,
                lastName = entity.lastName,
                phoneNumber = entity.phoneNumber,
                email = entity.email,
                dateOfBirth = entity.dateOfBirth,
                salaryEuros = entity.salaryEuros,
                notes = entity.notes,
                isFavorite = entity.isFavorite,
                createdAt = entity.createdAt,
                updatedAt = entity.updatedAt
            )
        }
    }
}

