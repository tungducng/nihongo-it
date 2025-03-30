package com.example.japanesitlearning.entity

import jakarta.persistence.Column
import jakarta.persistence.Entity
import jakarta.persistence.FetchType
import jakarta.persistence.GeneratedValue
import jakarta.persistence.Id
import jakarta.persistence.JoinColumn
import jakarta.persistence.ManyToOne
import jakarta.persistence.Table
import java.time.LocalDateTime
import java.util.*

@Entity
@Table(name = "practice_sessions")
data class PracticeSessionEntity(
    @Id
    @GeneratedValue(generator = "UUID")
    @Column(name = "session_id", updatable = false, nullable = false)
    val sessionId: UUID? = null,

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id")
    val user: UserEntity,

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "conversation_id")
    val conversation: ConversationEntity,

    @Column(name = "pronunciation_score")
    val pronunciationScore: Float?,

    @Column(name = "recorded_audio_url")
    val recordedAudioUrl: String?,

    @Column(name = "practice_date", nullable = false)
    val practiceDate: LocalDateTime = LocalDateTime.now(),

    @Column(name = "duration_seconds")
    val durationSeconds: Int?,

    @Column(name = "ai_feedback", columnDefinition = "TEXT")
    val aiFeedback: String?,

    @Column(name = "next_review_date")
    val nextReviewDate: LocalDateTime?
)