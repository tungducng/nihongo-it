package com.example.japanesitlearning.dto.vocabulary

import com.example.japanesitlearning.entity.JLPTLevel
import com.fasterxml.jackson.annotation.JsonFormat
import com.fasterxml.jackson.databind.annotation.JsonDeserialize
import com.fasterxml.jackson.databind.annotation.JsonSerialize
import com.fasterxml.jackson.datatype.jsr310.deser.LocalDateTimeDeserializer
import com.fasterxml.jackson.datatype.jsr310.ser.LocalDateTimeSerializer
import java.time.LocalDateTime
import java.util.*

// Response DTO for vocabulary retrieval
data class VocabularyDto(
    val vocabId: UUID,
    val hiragana: String,
    val kanji: String?,
    val katakana: String?,
    val meaning: String,
    val exampleSentence: String?,
    val audioPath: String?,
    val category: String?,
    val jlptLevel: JLPTLevel,
    val contentType: String,

    @JsonDeserialize(using = LocalDateTimeDeserializer::class)
    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd'T'HH:mm:ss")
    @JsonSerialize(using = LocalDateTimeSerializer::class)
    val createdAt: LocalDateTime,
    val createdBy: String,
    val isSaved: Boolean = false,
)
