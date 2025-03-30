package com.example.japanesitlearning.dto.vocabulary

data class VocabularyAiResponse(
    val hiragana: String,
    val meaning: String,
    val kanji: String? = null,
    val partOfSpeech: String? = null,
    val explanation: String? = null,
    val exampleSentence: String? = null,
    val exampleTranslation: String? = null,
    val relatedWords: List<String> = emptyList()
)