package com.example.learningservice.entity

import jakarta.persistence.*
import java.time.LocalDateTime
import java.util.*

@Entity
@Table(name = "feedback")
data class FeedbackEntity(
    @Id
    @GeneratedValue(generator = "UUID")
    @Column(name = "feedback_id", updatable = false, nullable = false)
    val feedbackId: UUID? = null,
    // Cross-service reference — no DB-level FK.
    @Column(name = "user_id", nullable = false)
    val userId: UUID,
    @Column(name = "content_type", nullable = false)
    val contentType: String, // 'conversation', 'vocabulary'
    @Column(name = "content", columnDefinition = "text")
    val content: String?,
    @Column(name = "content_id", nullable = false)
    val contentId: UUID,
    @Column(name = "created_at", nullable = false)
    val createdAt: LocalDateTime = LocalDateTime.now(),
)
