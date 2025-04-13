package com.example.nihongoit.entity

/**
 * Represents different types of questions for Japanese language learning
 */
enum class QuestionType {
    /**
     * Shows English meaning, asks for the corresponding Japanese word
     */
    MEANING_TO_WORD,
    
    /**
     * Shows Japanese word, asks for the corresponding English meaning
     */
    WORD_TO_MEANING,
    
    /**
     * Shows kanji, asks for the correct reading in hiragana
     */
    READING_SELECTION,
    
    /**
     * Shows a sentence with context, asks for word meaning
     */
    CONTEXT_SELECTION,
    
    /**
     * Shows audio, asks to select the correct word or meaning
     */
    LISTENING,
    
    /**
     * Grammar-related questions
     */
    GRAMMAR
} 