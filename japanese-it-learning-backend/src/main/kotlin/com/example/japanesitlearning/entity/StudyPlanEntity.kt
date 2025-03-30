package com.example.japanesitlearning.entity

import jakarta.persistence.Column
import jakarta.persistence.Entity
import jakarta.persistence.EnumType
import jakarta.persistence.Enumerated
import jakarta.persistence.FetchType
import jakarta.persistence.GeneratedValue
import jakarta.persistence.Id
import jakarta.persistence.JoinColumn
import jakarta.persistence.ManyToOne
import jakarta.persistence.Table
import java.time.LocalDateTime
import java.util.*

@Entity
@Table(name = "study_plans")
data class StudyPlanEntity(
    @Id
    @GeneratedValue(generator = "UUID")
    @Column(name = "plan_id", updatable = false, nullable = false)
    val planId: UUID? = null,

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id")
    val user: UserEntity,

    @Column(name = "target_jlpt_level", nullable = false)
    val targetJlptLevel: Int,

    @Column(name = "weekly_study_hours", nullable = false)
    val weeklyStudyHours: Int,

    @Column(name = "current_streak_days", nullable = false)
    val currentStreakDays: Int = 0,

    @Column(name = "last_study_date")
    val lastStudyDate: LocalDateTime?,

    @Column(name = "target_completion_date")
    val targetCompletionDate: LocalDateTime?,

    @Column(name = "learning_focus", nullable = false)
    @Enumerated(EnumType.STRING)
    val learningFocus: LearningFocus = LearningFocus.BALANCED,

    @Column(name = "is_active", nullable = false)
    val isActive: Boolean = true
)

enum class LearningFocus {
    READING,
    LISTENING,
    SPEAKING,
    WRITING,
    VOCABULARY,
    GRAMMAR,
    BALANCED
}