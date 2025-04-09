package com.example.japanesitlearning.entity

import jakarta.persistence.*
import java.time.LocalDateTime
import java.util.*

@Entity
@Table(name = "flashcard_decks")
data class FlashcardDeckEntity(
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @Column(name = "deck_id")
    val deckId: UUID = UUID.randomUUID(),

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id")
    val user: UserEntity,

    @Column(name = "title", nullable = false)
    val title: String,

    @Column(name = "description", columnDefinition = "text")
    val description: String?,

    @Enumerated(EnumType.STRING)
    @Column(name = "category", nullable = false)
    val category: FlashcardCategory,

    @Column(name = "is_public")
    val isPublic: Boolean = false,

    @Column(name = "total_cards")
    val totalCards: Int = 0,

    @Column(name = "last_studied")
    val lastStudied: LocalDateTime?,

    @Column(name = "created_at")
    val createdAt: LocalDateTime = LocalDateTime.now(),

    @Column(name = "updated_at")
    val updatedAt: LocalDateTime = LocalDateTime.now(),
)

enum class FlashcardCategory {
    VOCABULARY,
    GRAMMAR,
    CONVERSATION,
    CUSTOM,
} 
