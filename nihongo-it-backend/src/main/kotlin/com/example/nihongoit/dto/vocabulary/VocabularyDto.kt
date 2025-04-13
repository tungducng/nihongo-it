package com.example.nihongoit.dto.vocabulary

import com.example.nihongoit.entity.JlptLevel
import com.fasterxml.jackson.annotation.JsonFormat
import com.fasterxml.jackson.annotation.JsonProperty
import com.fasterxml.jackson.databind.annotation.JsonDeserialize
import com.fasterxml.jackson.databind.annotation.JsonSerialize
import com.fasterxml.jackson.datatype.jsr310.deser.LocalDateTimeDeserializer
import com.fasterxml.jackson.datatype.jsr310.ser.LocalDateTimeSerializer
import java.time.LocalDateTime
import java.util.*

// Response DTO for vocabulary retrieval
data class VocabularyDto(
    @JsonProperty("vocabId")
    val vocabId: UUID,

    @JsonProperty("hiragana")
    val hiragana: String?,

    @JsonProperty("kanji")
    val kanji: String?,

    @JsonProperty("katakana")
    val katakana: String?,

    @JsonProperty("meaning")
    val meaning: String,

    @JsonProperty("exampleSentence")
    val exampleSentence: String?,

    @JsonProperty("exampleSentenceTranslation")
    val exampleSentenceTranslation: String?,

    @JsonProperty("audioPath")
    val audioPath: String?,

    @JsonProperty("category")
    val category: String?,

    @JsonProperty("jlptLevel")
    val jlptLevel: JlptLevel,

    @JsonProperty("createdAt")
    @JsonDeserialize(using = LocalDateTimeDeserializer::class)
    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd HH:mm")
    @JsonSerialize(using = LocalDateTimeSerializer::class)
    val createdAt: LocalDateTime,

    @JsonProperty("createdBy")
    val createdBy: String,

    @JsonProperty("isSaved")
    val isSaved: Boolean = false,
)
