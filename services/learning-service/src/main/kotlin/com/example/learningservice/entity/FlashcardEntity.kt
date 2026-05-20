package com.example.learningservice.entity

import com.example.common.entity.AbstractAuditEntity
import jakarta.persistence.*
import java.time.LocalDateTime
import java.util.*

@Entity
@Table(name = "flashcards")
data class FlashcardEntity(
    @Id
    @GeneratedValue(generator = "UUID")
    @Column(name = "flashcard_id", updatable = false, nullable = false)
    val flashcardId: UUID? = null,
    // Cross-service reference — no DB-level FK. The user record lives in
    // user-service; learning-service stores only the UUID.
    @Column(name = "user_id", nullable = false)
    val userId: UUID,
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "vocabulary_id")
    val vocabulary: VocabularyEntity? = null,
    @Column(name = "front_text", columnDefinition = "text")
    var frontText: String,
    @Column(name = "back_text", columnDefinition = "text")
    var backText: String,
    // FSRS algorithm fields
    @Column(name = "difficulty", nullable = true)
    var difficulty: Double? = null,
    @Column(name = "stability", nullable = true)
    var stability: Double? = null,
    @Column(name = "state")
    var state: Int = 0, // 0=New, 1=Learning, 2=Review, 3=Relearning
    @Column(name = "elapsed_days")
    var elapsedDays: Double = 0.0,
    @Column(name = "scheduled_days")
    var scheduledDays: Double = 0.0,
    @Column(name = "due")
    var due: LocalDateTime = LocalDateTime.now(),
    @Column(name = "reps")
    var reps: Int = 0,
    @Column(name = "lapses")
    var lapses: Int = 0,
    @Column(name = "is_suspended")
    var isSuspended: Boolean = false,
    @OneToMany(mappedBy = "flashcard", cascade = [CascadeType.ALL], orphanRemoval = true)
    var reviewLogs: MutableList<ReviewLogEntity> = mutableListOf(),
) : AbstractAuditEntity()
