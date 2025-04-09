package com.example.japanesitlearning.entity

import jakarta.persistence.*
import java.time.LocalDateTime
import java.util.*

@Entity
@Table(name = "achievements")
data class AchievementEntity(
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @Column(name = "achievement_id")
    val achievementId: UUID = UUID.randomUUID(),

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id")
    val user: UserEntity,

    @Column(name = "title", nullable = false)
    val title: String,

    @Column(name = "description", columnDefinition = "text")
    val description: String?,

    @Enumerated(EnumType.STRING)
    @Column(name = "type", nullable = false)
    val type: AchievementType,

    @Column(name = "points")
    val points: Int = 0,

    @Column(name = "progress")
    val progress: Int = 0,

    @Column(name = "target")
    val target: Int = 0,

    @Column(name = "is_completed")
    val isCompleted: Boolean = false,

    @Column(name = "completed_at")
    val completedAt: LocalDateTime?,

    @Column(name = "created_at")
    val createdAt: LocalDateTime = LocalDateTime.now(),
)

enum class AchievementType {
    STUDY_STREAK,
    LESSON_COMPLETION,
    QUIZ_PERFORMANCE,
    VOCABULARY_MASTERY,
    GRAMMAR_MASTERY,
    CONVERSATION_PRACTICE,
    DAILY_GOAL,
    LEVEL_UP,
} 
