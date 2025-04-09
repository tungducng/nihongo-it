package com.example.japanesitlearning.dto.vocabulary

import com.example.japanesitlearning.entity.JLPTLevel
import com.fasterxml.jackson.annotation.JsonInclude
import com.fasterxml.jackson.annotation.JsonProperty
import jakarta.validation.constraints.Pattern
import jakarta.validation.constraints.Size

@JsonInclude(JsonInclude.Include.NON_NULL)
data class UpdateVocabularyRequestDto(
    @get:Size(max = 100, message = "Hiragana cannot exceed 100 characters")
    @JsonProperty("hiragana")
    val hiragana: String?,

    @get:Size(max = 255, message = "Meaning cannot exceed 255 characters")
    @JsonProperty("meaning")
    val meaning: String?,

    @get:Size(max = 100, message = "Kanji cannot exceed 100 characters")
    @JsonProperty("kanji")
    val kanji: String? = null,

    @get:Size(max = 100, message = "Katakana cannot exceed 100 characters")
    @JsonProperty("katakana")
    val katakana: String? = null,

    @get:Size(max = 500, message = "Example sentence cannot exceed 500 characters")
    @JsonProperty("exampleSentence")
    val exampleSentence: String? = null,

    @JsonProperty("audioPath")
    val audioPath: String? = null,

    @get:Size(max = 50, message = "Category cannot exceed 50 characters")
    @JsonProperty("category")
    val category: String? = null,

    @JsonProperty("jlptLevel")
    val jlptLevel: JLPTLevel?,

    @get:Size(max = 20, message = "Content type cannot exceed 20 characters")
    @get:Pattern(regexp = "^(vocabulary|grammar|conversation)$", message = "Content type must be one of: vocabulary, grammar, conversation")
    @JsonProperty("contentType")
    val contentType: String?,
) 
