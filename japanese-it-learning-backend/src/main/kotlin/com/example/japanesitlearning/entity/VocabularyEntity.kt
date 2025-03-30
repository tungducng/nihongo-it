package com.example.japanesitlearning.entity

import jakarta.persistence.Column
import jakarta.persistence.Entity
import jakarta.persistence.FetchType
import jakarta.persistence.GeneratedValue
import jakarta.persistence.Id
import jakarta.persistence.JoinColumn
import jakarta.persistence.ManyToOne
import jakarta.persistence.Table
import java.util.*

@Entity
@Table(name = "vocabularies")
data class VocabularyEntity(
    @Id
    @GeneratedValue(generator = "UUID")
    @Column(name = "vocabulary_id", updatable = false, nullable = false)
    val vocabularyId: UUID? = null,

    @Column(name = "kanji")
    val kanji: String?,

    @Column(name = "hiragana", nullable = false)
    val hiragana: String,

    @Column(name = "meaning", nullable = false)
    val meaning: String,

    @Column(name = "example_sentence", columnDefinition = "TEXT")
    val exampleSentence: String?,

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "lesson_id")
    val lesson: LessonEntity,

    @Column(name = "part_of_speech")
    val partOfSpeech: String?,

    @Column(name = "it_context", nullable = false)
    val itContext: Boolean = false,

    @Column(name = "audio_url")
    val audioUrl: String?,

    @Column(name = "similar_words", columnDefinition = "TEXT")
    val similarWords: String?,

    @Column(name = "mnemonic_hint")
    val mnemonicHint: String?,

    @Column(name = "jlpt_level", nullable = false)
    val jlptLevel: Int
)