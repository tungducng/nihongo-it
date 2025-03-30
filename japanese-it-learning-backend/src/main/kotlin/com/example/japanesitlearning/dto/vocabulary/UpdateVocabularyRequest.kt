package com.example.japanesitlearning.dto.vocabulary

import com.fasterxml.jackson.annotation.JsonCreator
import java.util.*

data class UpdateVocabularyRequest @JsonCreator constructor(
    val kanji: String?,
    val hiragana: String,
    val meaning: String,
    val exampleSentence: String?,
    val lessonId: UUID,
    val partOfSpeech: String?,
    val itContext: Boolean = false,
    val audioUrl: String?,
    val similarWords: String?,
    val mnemonicHint: String?,
    val jlptLevel: Int
)
