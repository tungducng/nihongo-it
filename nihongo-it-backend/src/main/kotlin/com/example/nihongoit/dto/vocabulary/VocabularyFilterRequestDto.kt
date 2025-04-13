package com.example.nihongoit.dto.vocabulary

import com.example.nihongoit.entity.JlptLevel

// DTO for filtering vocabulary
data class VocabularyFilterRequestDto(
    val jlptLevel: JlptLevel? = null,
    val category: String? = null,
    val keyword: String? = null,
    val page: Int = 0,
    val size: Int = 20,
)
