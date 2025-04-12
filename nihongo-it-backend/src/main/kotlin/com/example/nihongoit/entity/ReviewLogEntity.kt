package com.example.nihongoit.entity

import jakarta.persistence.Column
import jakarta.persistence.Entity
import jakarta.persistence.FetchType
import jakarta.persistence.GeneratedValue
import jakarta.persistence.GenerationType
import jakarta.persistence.Id
import jakarta.persistence.JoinColumn
import jakarta.persistence.ManyToOne
import jakarta.persistence.Table
import java.time.LocalDateTime
import java.util.*

// Entity cho ReviewLog
@Entity
@Table(name = "review_logs")
data class ReviewLogEntity(
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @Column(name = "review_log_id")
    val reviewLogId: UUID = UUID.randomUUID(),

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "flashcard_id", nullable = false)
    val flashcard: FlashcardEntity,

    @Column(name = "user_id", nullable = false)
    val userId: UUID,

    @Column(name = "timestamp")
    val timestamp: LocalDateTime = LocalDateTime.now(),

    @Column(name = "rating", nullable = false)
    val rating: Int,

    @Column(name = "state", nullable = false)
    val state: Int,

    @Column(name = "elapsed_days", nullable = false)
    val elapsedDays: Double
)