package com.example.nihongoit.entity

import jakarta.persistence.*
import java.time.LocalDateTime
import java.util.*

@Entity
@Table(name = "quiz_attempts")
data class QuizAttemptEntity(
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @Column(name = "attempt_id")
    val attemptId: UUID = UUID.randomUUID(),

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "quiz_id", nullable = false)
    val quiz: QuizEntity,

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    val user: UserEntity,

    @Column(name = "score")
    val score: Int,
    
    @Column(name = "max_score")
    val maxScore: Int,
    
    @Column(name = "score_percentage")
    val scorePercentage: Double,
    
    @Column(name = "passed")
    val passed: Boolean,
    
    @Column(name = "time_spent_seconds")
    val timeSpentSeconds: Int,
    
    @Column(name = "completed")
    val completed: Boolean = true,
    
    @OneToMany(mappedBy = "quizAttempt", cascade = [CascadeType.ALL], orphanRemoval = true)
    val questionResponses: MutableList<QuizResponseEntity> = mutableListOf(),

    @Column(name = "started_at")
    val startedAt: LocalDateTime = LocalDateTime.now(),

    @Column(name = "completed_at")
    val completedAt: LocalDateTime? = null,
    
    @Column(name = "feedback", columnDefinition = "text")
    val feedback: String? = null
)

@Entity
@Table(name = "quiz_responses")
data class QuizResponseEntity(
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @Column(name = "response_id")
    val responseId: UUID = UUID.randomUUID(),

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "attempt_id", nullable = false)
    val quizAttempt: QuizAttemptEntity,

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "question_id", nullable = false)
    val question: QuizQuestionEntity,

    @Column(name = "user_answer", columnDefinition = "text")
    val userAnswer: String?,
    
    @Column(name = "is_correct")
    val isCorrect: Boolean,
    
    @Column(name = "points_earned")
    val pointsEarned: Int,
    
    @Column(name = "time_spent_seconds")
    val timeSpentSeconds: Int? = null,
    
    @Column(name = "answered_at")
    val answeredAt: LocalDateTime = LocalDateTime.now()
) 