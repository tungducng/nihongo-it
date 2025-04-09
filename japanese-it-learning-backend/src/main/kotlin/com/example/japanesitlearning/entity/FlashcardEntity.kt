package com.example.japanesitlearning.entity

import jakarta.persistence.*
import java.time.LocalDateTime
import java.util.*

@Entity
@Table(name = "flashcards")
data class FlashcardEntity(
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @Column(name = "card_id")
    val cardId: UUID = UUID.randomUUID(),

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "deck_id")
    val deck: FlashcardDeckEntity,

    @Column(name = "front_content", columnDefinition = "text", nullable = false)
    val frontContent: String,

    @Column(name = "back_content", columnDefinition = "text", nullable = false)
    val backContent: String,

    @Column(name = "image_url")
    val imageUrl: String?,

    @Column(name = "audio_url")
    val audioUrl: String?,

    @Column(name = "example_sentence", columnDefinition = "text")
    val exampleSentence: String?,

    @Column(name = "notes", columnDefinition = "text")
    val notes: String?,

    @Column(name = "difficulty_level")
    val difficultyLevel: Int = 0,

    @Column(name = "review_count")
    val reviewCount: Int = 0,

    @Column(name = "last_reviewed")
    val lastReviewed: LocalDateTime?,

    @Column(name = "next_review")
    val nextReview: LocalDateTime?,

    @Column(name = "order_index")
    val orderIndex: Int = 0,

    @Column(name = "created_at")
    val createdAt: LocalDateTime = LocalDateTime.now(),

    @Column(name = "updated_at")
    val updatedAt: LocalDateTime = LocalDateTime.now(),
) 
