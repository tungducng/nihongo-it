package com.example.nihongoit.entity

import jakarta.persistence.*
import org.hibernate.annotations.CreationTimestamp
import org.hibernate.annotations.UpdateTimestamp
import java.time.LocalDateTime
import java.util.*

/**
 * Entity representing a standalone question for testing Japanese language knowledge
 */
@Entity
@Table(name = "questions")
data class QuestionEntity(
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    val questionId: UUID = UUID.randomUUID(),

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "creator_id", nullable = false)
    val creator: UserEntity,
    
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "vocabulary_id", nullable = false)
    val vocabulary: VocabularyEntity,

    @Column(nullable = false)
    val questionText: String,
    
    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    val questionType: QuestionType,
    
    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    val level: JlptLevel,
    
    @Column(name = "option_a", nullable = false, columnDefinition = "text")
    val optionA: String,

    @Column(name = "option_b", nullable = false, columnDefinition = "text")
    val optionB: String,

    @Column(name = "option_c", nullable = false, columnDefinition = "text")
    val optionC: String,

    @Column(name = "option_d", nullable = false, columnDefinition = "text")
    val optionD: String,

    @Column(name = "correct_option", nullable = false)
    val correctOption: String, // "A", "B", "C", or "D"
    
    @Column(nullable = false, updatable = false)
    val createdAt: LocalDateTime = LocalDateTime.now(),

    @Column(nullable = false)
    val updatedAt: LocalDateTime = LocalDateTime.now()
) 