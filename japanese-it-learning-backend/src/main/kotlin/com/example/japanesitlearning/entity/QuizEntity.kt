package com.example.japanesitlearning.entity

import jakarta.persistence.Column
import jakarta.persistence.Entity
import jakarta.persistence.FetchType
import jakarta.persistence.GeneratedValue
import jakarta.persistence.Id
import jakarta.persistence.JoinColumn
import jakarta.persistence.ManyToOne
import jakarta.persistence.Table
import java.util.*

@Entity
@Table(name = "quizzes")
data class QuizEntity(
    @Id
    @GeneratedValue(generator = "UUID")
    @Column(name = "quiz_id", updatable = false, nullable = false)
    val quizId: UUID? = null,

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "lesson_id")
    val lesson: LessonEntity,

    @Column(name = "question", nullable = false)
    val question: String,

    @Column(name = "correct_answer", nullable = false)
    val correctAnswer: String,

    @Column(name = "wrong_answers", nullable = false)
    val wrongAnswers: Array<String>,

    @Column(name = "quiz_type", nullable = false)
    val quizType: Int // 1: Vocabulary, 2: Grammar, 3: Reading
)