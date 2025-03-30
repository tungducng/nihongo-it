package com.example.japanesitlearning.dto.vocabulary

import java.util.*

data class VocabularyResponse(
    val id: UUID,
    val kanji: String?,
    val hiragana: String,
    val meaning: String,
    val exampleSentence: String?,
    val lessonId: UUID,
    val lessonName: String,
    val partOfSpeech: String?,
    val itContext: Boolean,
    val audioUrl: String?,
    val similarWords: String?,
    val mnemonicHint: String?,
    val jlptLevel: Int
)