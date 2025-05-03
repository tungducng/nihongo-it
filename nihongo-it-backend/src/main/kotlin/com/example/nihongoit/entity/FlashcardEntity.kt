package com.example.nihongoit.entity

import jakarta.persistence.*
import java.time.LocalDateTime
import java.util.*

@Entity
@Table(name = "flashcards")
data class FlashcardEntity(
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @Column(name = "flashcard_id")
    val flashCardId: UUID? = UUID.randomUUID(),

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    val user: UserEntity,

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "vocabulary_id")
    val vocabulary: VocabularyEntity? = null,

    @Column(name = "front_text", nullable = false, columnDefinition = "text")
    var frontText: String ?= null,

    @Column(name = "back_text", nullable = false, columnDefinition = "text")
    var backText: String ?= null,

    // FSRS algorithm fields
    @Column(name = "difficulty")
    var difficulty: Double = 0.3, // Item difficulty (0.1 to 1.0)

    @Column(name = "stability")
    var stability: Double = 0.5, // Memory stability (how well retained)

    @Column(name = "state")
    var state: Int = 0, // 0=New, 1=Learning, 2=Review, 3=Relearning

    @Column(name = "elapsed_days")
    var elapsedDays: Double = 0.0, // Days since last review

    @Column(name = "scheduled_days")
    var scheduledDays: Double = 1.0, // Days scheduled before next review

    @Column(name = "due")
    var due: LocalDateTime = LocalDateTime.now(), // Next review date

    @Column(name = "reps")
    var reps: Int = 0, // Number of repetitions

    @Column(name = "lapses")
    var lapses: Int = 0, // Number of times forgotten

    @Column(name = "is_suspended")
    var isSuspended: Boolean = false, // Whether card is temporarily suspended

    @OneToMany(mappedBy = "flashcard", cascade = [CascadeType.ALL], orphanRemoval = true)
    var reviewLogs: MutableList<ReviewLogEntity> = mutableListOf(),

    @Column(name = "created_at")
    val createdAt: LocalDateTime = LocalDateTime.now(),

    @Column(name = "updated_at")
    var updatedAt: LocalDateTime = LocalDateTime.now()
)
