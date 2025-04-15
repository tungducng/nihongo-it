package com.example.nihongoit.entity

import jakarta.persistence.*
import java.time.LocalDateTime
import java.util.*

@Entity
@Table(name = "vocabulary")
data class VocabularyEntity(
    @Id
    @GeneratedValue(generator = "UUID")
    @Column(name = "vocab_id", updatable = false, nullable = false)
    val vocabId: UUID? = null,

    @Column(name = "kanji")
    val kanji: String?,

    @Column(name = "katakana")
    val katakana: String?,

    @Column(name = "hiragana")
    val hiragana: String?,

    @Column(name = "meaning", length = 255)
    val meaning: String, //vietnamese meaning

    @Column(name = "example_sentence", columnDefinition = "text")
    val exampleSentence: String?,

    @Column(name = "example_sentence_translation", columnDefinition = "text")
    val exampleSentenceTranslation: String?, //vietnamese meaning

    @Column(name = "audio_path")
    val audioPath: String?,

    @Column(name = "category", length = 50)
    val category: String?,

    @Enumerated(EnumType.STRING)
    @Column(name = "jlpt_level")
    val jlptLevel: JlptLevel,

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "created_by", referencedColumnName = "user_id")
    val createdBy: UserEntity?,

    @Column(name = "created_at")
    val createdAt: LocalDateTime?,

    @OneToMany(mappedBy = "vocabulary", cascade = [CascadeType.ALL])
    val flashcards: MutableList<FlashcardEntity> = mutableListOf(),

    @ManyToMany
    @JoinTable(
        name = "saved_vocabulary",
        joinColumns = [JoinColumn(name = "vocab_id")],
        inverseJoinColumns = [JoinColumn(name = "user_id")],
    )
    val savedByUsers: MutableSet<UserEntity> = mutableSetOf(),
)
