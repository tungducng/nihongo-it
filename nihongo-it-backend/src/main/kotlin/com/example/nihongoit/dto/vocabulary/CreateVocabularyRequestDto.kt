package com.example.nihongoit.dto.vocabulary

import com.fasterxml.jackson.annotation.JsonInclude
import com.fasterxml.jackson.annotation.JsonProperty
import jakarta.validation.constraints.NotBlank
import jakarta.validation.constraints.NotNull
import jakarta.validation.constraints.Pattern
import jakarta.validation.constraints.Size

@JsonInclude(JsonInclude.Include.NON_NULL)
data class CreateVocabularyRequestDto(
    @get:NotBlank
    @get:Size(max = 100, message = "Hiragana cannot exceed 100 characters")
    @JsonProperty("hiragana")
    val hiragana: String? = null,

    @get:NotBlank(message = "Meaning is required")
    @get:Size(max = 255, message = "Meaning cannot exceed 255 characters")
    @JsonProperty("meaning")
    val meaning: String,

    @get:Size(max = 100, message = "Kanji cannot exceed 100 characters")
    @JsonProperty("kanji")
    val kanji: String? = null,

    @get:Size(max = 100, message = "Katakana cannot exceed 100 characters")
    @JsonProperty("katakana")
    val katakana: String? = null,

    @get:Size(max = 500, message = "Example sentence cannot exceed 500 characters")
    @JsonProperty("exampleSentence")
    val exampleSentence: String? = null, //example sentence

    @get:Size(max = 500, message = "Example sentence translation cannot exceed 500 characters")
    @JsonProperty("exampleSentenceTranslation")
    val exampleSentenceTranslation: String? = null, //vietnamese meaning

    @JsonProperty("audioPath")
    val audioPath: String? = null,

    @get:Size(max = 50, message = "Category cannot exceed 50 characters")
    @JsonProperty("category")
    val category: String? = null,

    @get:NotNull(message = "JLPT level is required")
    @get:Pattern(regexp = "^(N5|N4|N3|N2|N1)$", message = "JLPT level must be one of: N5, N4, N3, N2, N1")
    @JsonProperty("jlptLevel")
    val jlptLevel: String,
)
