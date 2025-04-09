package com.example.japanesitlearning.entity

import jakarta.persistence.*
import java.time.LocalDateTime
import java.util.*

@Entity
@Table(name = "lessons")
data class LessonEntity(
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @Column(name = "lesson_id")
    val lessonId: UUID = UUID.randomUUID(),

    @Column(name = "title", nullable = false)
    val title: String,

    @Column(name = "description", columnDefinition = "text")
    val description: String?,

    @Column(name = "content", columnDefinition = "text")
    val content: String?,

    @Enumerated(EnumType.STRING)
    @Column(name = "level", nullable = false)
    val level: JLPTLevel,

    @Enumerated(EnumType.STRING)
    @Column(name = "category", nullable = false)
    val category: LessonCategory,

    @Enumerated(EnumType.STRING)
    @Column(name = "type", nullable = false)
    val type: LessonType,

    @Column(name = "duration_minutes")
    val durationMinutes: Int?,

    @Column(name = "order_index")
    val orderIndex: Int = 0,

    @Column(name = "is_active")
    val isActive: Boolean = true,

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "created_by")
    val createdBy: UserEntity,

    @Column(name = "created_at")
    val createdAt: LocalDateTime = LocalDateTime.now(),

    @Column(name = "updated_at")
    val updatedAt: LocalDateTime = LocalDateTime.now(),
)

enum class LessonCategory {
    PROGRAMMING,
    NETWORKING,
    DATABASE,
    AI,
    CLOUD,
    SECURITY,
    DEVOPS,
    OTHER,
}

enum class LessonType {
    VOCABULARY,
    GRAMMAR,
    CONVERSATION,
}
