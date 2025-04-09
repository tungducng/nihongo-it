package com.example.japanesitlearning.dto.vocabulary

import com.example.japanesitlearning.entity.JLPTLevel

// DTO for filtering vocabulary
data class VocabularyFilterRequestDto(
    val jlptLevel: JLPTLevel? = null,
    val category: String? = null,
    val contentType: String? = null,
    val keyword: String? = null,
    val page: Int = 0,
    val size: Int = 20,
)
