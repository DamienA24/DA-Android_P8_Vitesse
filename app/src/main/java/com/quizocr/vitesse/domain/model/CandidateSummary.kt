package com.quizocr.vitesse.domain.model

import androidx.room.ColumnInfo
import com.quizocr.vitesse.data.entity.CandidateEntity

data class CandidateSummary(
    val id: Int,

    @ColumnInfo(name = "photo_uri")
    val photoUri: String? = null,

    @ColumnInfo(name = "first_name")
    val firstName: String,

    @ColumnInfo(name = "last_name")
    val lastName: String,

    val notes: String? = null,

    @ColumnInfo(name = "is_favorite")
    val isFavorite: Boolean
)
