package com.example.japanesitlearning.dto.vocabulary

data class VocabularyPageResponse(
    val content: List<VocabularyResponse>,
    val page: Int,
    val size: Int,
    val totalElements: Long,
    val totalPages: Int
)