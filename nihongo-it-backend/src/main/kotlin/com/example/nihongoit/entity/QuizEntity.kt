package com.example.nihongoit.entity

import jakarta.persistence.*
import java.time.LocalDateTime
import java.util.*

@Entity
@Table(name = "quizzes")
data class QuizEntity(
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @Column(name = "quiz_id")
    val quizId: UUID = UUID.randomUUID(),

    @Column(name = "title", nullable = false)
    val title: String,

    @Column(name = "description", columnDefinition = "text")
    val description: String?,

    @Enumerated(EnumType.STRING)
    @Column(name = "level", nullable = false)
    val level: JlptLevel,

    @Enumerated(EnumType.STRING)
    @Column(name = "type", nullable = false)
    val type: QuizType,
    
    @Column(name = "time_limit_minutes")
    val timeLimitMinutes: Int? = null,
    
    @Column(name = "passing_score")
    val passingScore: Int = 70, // Default passing score is 70%
    
    @Column(name = "is_active")
    val isActive: Boolean = true,
    
    @Column(name = "difficulty_level")
    val difficultyLevel: Int = 1, // 1=Beginner, 2=Intermediate, 3=Advanced

    @OneToMany(mappedBy = "quiz", cascade = [CascadeType.ALL], orphanRemoval = true)
    val questions: MutableList<QuizQuestionEntity> = mutableListOf(),

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "created_by")
    val createdBy: UserEntity,

    @Column(name = "created_at")
    val createdAt: LocalDateTime = LocalDateTime.now(),

    @Column(name = "updated_at")
    val updatedAt: LocalDateTime = LocalDateTime.now()
)

@Entity
@Table(name = "quiz_questions")
data class QuizQuestionEntity(
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @Column(name = "question_id")
    val questionId: UUID = UUID.randomUUID(),

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "quiz_id", nullable = false)
    val quiz: QuizEntity,

    @Column(name = "question_text", nullable = false, columnDefinition = "text")
    val questionText: String,
    
    @Column(name = "question_image_url")
    val questionImageUrl: String? = null,
    
    @Column(name = "question_audio_url")
    val questionAudioUrl: String? = null,

    @Column(name = "option_a", columnDefinition = "text")
    val optionA: String?,

    @Column(name = "option_b", columnDefinition = "text")
    val optionB: String?,

    @Column(name = "option_c", columnDefinition = "text")
    val optionC: String?,

    @Column(name = "option_d", columnDefinition = "text")
    val optionD: String?,

    @Column(name = "correct_answer", nullable = false)
    val correctAnswer: String, // "A", "B", "C", "D" or for input questions: the correct text
    
    @Column(name = "question_type")
    val questionType: String = "MULTIPLE_CHOICE", // MULTIPLE_CHOICE, TRUE_FALSE, TEXT_INPUT, MATCHING

    @Column(name = "explanation", columnDefinition = "text")
    val explanation: String?,
    
    @Column(name = "points")
    val points: Int = 1,
    
    @Column(name = "order_index")
    val orderIndex: Int = 0
)

enum class QuizType {
    VOCABULARY,
    GRAMMAR,
    CONVERSATION,
} 