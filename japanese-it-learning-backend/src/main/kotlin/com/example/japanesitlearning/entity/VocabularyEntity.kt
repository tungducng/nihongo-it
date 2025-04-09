package com.example.japanesitlearning.entity

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

    @Column(name = "hiragana", nullable = false)
    val hiragana: String,

    @Column(name = "meaning", nullable = false, length = 255)
    val meaning: String,

    @Column(name = "example_sentence", columnDefinition = "text")
    val exampleSentence: String?,

    @Column(name = "audio_path")
    val audioPath: String?,

    @Column(name = "category", length = 50)
    val category: String?,

    @Enumerated(EnumType.STRING)
    @Column(name = "jlpt_level")
    val jlptLevel: JLPTLevel,

    @Column(name = "content_type", length = 20)
    val contentType: String, // vocabulary, grammar, conversation

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "created_by", referencedColumnName = "user_id")
    val createdBy: UserEntity,

    @Column(name = "created_at")
    val createdAt: LocalDateTime = LocalDateTime.now(),

    @ManyToMany
    @JoinTable(
        name = "saved_vocabulary",
        joinColumns = [JoinColumn(name = "vocab_id")],
        inverseJoinColumns = [JoinColumn(name = "user_id")],
    )
    val savedByUsers: MutableSet<UserEntity> = mutableSetOf(),
)
