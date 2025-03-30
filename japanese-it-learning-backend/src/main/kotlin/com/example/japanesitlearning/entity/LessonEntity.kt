package com.example.japanesitlearning.entity

import jakarta.persistence.Column
import jakarta.persistence.Entity
import jakarta.persistence.EnumType
import jakarta.persistence.Enumerated
import jakarta.persistence.GeneratedValue
import jakarta.persistence.Id
import jakarta.persistence.Table
import java.util.*

@Entity
@Table(name = "lessons")
data class LessonEntity(
    @Id
    @GeneratedValue(generator = "UUID")
    @Column(name = "lesson_id", updatable = false, nullable = false)
    val lessonId: UUID? = null,

    @Column(name = "lesson_name", nullable = false)
    val lessonName: String,

    @Enumerated(EnumType.STRING)
    @Column(name = "category", nullable = false)
    val category: LessonCategory,

    @Column(name = "lesson_description")
    val description: String?,

    @Column(name = "lesson_level", nullable = false)
    val level: Int, // 1: N5, 2: N4, 3: N3, 4: N2, 5: N1

    @Column(name = "order_index", nullable = false)
    val orderIndex: Int
)