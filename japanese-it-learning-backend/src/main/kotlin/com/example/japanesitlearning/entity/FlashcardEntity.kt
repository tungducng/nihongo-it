package com.example.japanesitlearning.entity

import jakarta.persistence.*
import java.time.LocalDateTime
import java.util.*

@Entity
@Table(name = "flashcards")
data class FlashcardEntity(
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @Column(name = "flashcard_id")
    val flashCardId: UUID = UUID.randomUUID(),

    @Column(name = "user_id", nullable = false)
    val userId: UUID,

    @Column(name = "front_text", nullable = false)
    var frontText: String,

    @Column(name = "back_text", nullable = false)
    var backText: String,

    @Column(name = "notes")
    var notes: String? = null,

    @Column(name = "tags", columnDefinition = "jsonb")
    var tags: List<String> = listOf(),

    @Column(name = "created_at")
    var createdAt: LocalDateTime = LocalDateTime.now(),

    // Các trường FSRS
    @Column(name = "state")
    var state: Int = 0,

    @Column(name = "due")
    var due: LocalDateTime = LocalDateTime.now(),

    @Column(name = "stability")
    var stability: Double = 0.0,

    @Column(name = "difficulty")
    var difficulty: Double = 0.3,

    @Column(name = "elapsed_days")
    var elapsedDays: Double = 0.0,

    @Column(name = "scheduled_days")
    var scheduledDays: Double = 0.0,

    @Column(name = "reps")
    var reps: Int = 0,

    @Column(name = "lapses")
    var lapses: Int = 0,

    @OneToMany(mappedBy = "flashcard", cascade = [CascadeType.ALL], fetch = FetchType.LAZY)
    var reviewLogs: MutableList<ReviewLogEntity> = mutableListOf()
)
