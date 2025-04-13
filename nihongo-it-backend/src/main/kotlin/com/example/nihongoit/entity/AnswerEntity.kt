package com.example.nihongoit.entity

import jakarta.persistence.*
import org.hibernate.annotations.CreationTimestamp
import java.time.LocalDateTime
import java.util.*

/**
 * Entity representing a user's answer to a question
 */
@Entity
@Table(name = "answers")
data class AnswerEntity(
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    val answerId: UUID = UUID.randomUUID(),

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    val user: UserEntity,

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "question_id", nullable = false)
    val question: QuestionEntity,

    @Column(name = "selected_option")
    val selectedOption: String, // "A", "B", "C", or "D"
    
    @Column(name = "is_correct")
    val isCorrect: Boolean,
    
    @Column(name = "points_earned")
    val pointsEarned: Int = if (isCorrect) 1 else 0,
    
    @CreationTimestamp
    @Column(nullable = false, updatable = false)
    val answeredAt: LocalDateTime = LocalDateTime.now()
) 