package com.example.nihongoit.dto.vocabulary

import com.example.nihongoit.entity.JlptLevel
import com.fasterxml.jackson.annotation.JsonProperty

// DTO for filtering vocabulary
data class VocabularyFilterRequestDto(
    @JsonProperty("keyword")
    val keyword: String? = null,

    @JsonProperty("jlptLevel")
    val jlptLevel: JlptLevel? = null,

    @JsonProperty("topicName")
    val topicName: String? = null,

    @JsonProperty("page")
    val page: Int = 0,

    @JsonProperty("size")
    val size: Int = 10,

    @JsonProperty("sort")
    val sort: String? = null
)