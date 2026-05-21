package com.example.learningservice.entity

import com.example.common.entity.AbstractAuditEntity
import jakarta.persistence.Column
import jakarta.persistence.Entity
import jakarta.persistence.Id
import jakarta.persistence.Table
import java.time.LocalDateTime
import java.util.UUID

/**
 * Per-user learning progress — streak counter, lifetime points, daily goal.
 *
 * `userId` is a cross-service reference (no FK to a local users table; the
 * canonical user record lives in user-service). One row per user, created
 * lazily the first time the user submits a flashcard review.
 */
@Entity
@Table(name = "user_progress")
data class UserProgressEntity(
    @Id
    @Column(name = "user_id", nullable = false, updatable = false)
    val userId: UUID,
    @Column(name = "streak_count", nullable = false)
    var streakCount: Int = 0,
    @Column(name = "last_study_date")
    var lastStudyDate: LocalDateTime? = null,
    @Column(name = "points", nullable = false)
    var points: Int = 0,
    @Column(name = "daily_goal_minutes", nullable = false)
    var dailyGoalMinutes: Int = 15,
) : AbstractAuditEntity()
