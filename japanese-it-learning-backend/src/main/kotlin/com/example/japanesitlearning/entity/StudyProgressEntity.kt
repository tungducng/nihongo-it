package com.example.japanesitlearning.entity

import jakarta.persistence.*
import java.time.LocalDateTime
import java.util.*

@Entity
@Table(name = "study_progress")
data class StudyProgressEntity(
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @Column(name = "progress_id")
    val progressId: UUID = UUID.randomUUID(),

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id")
    val user: UserEntity,

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "lesson_id")
    val lesson: LessonEntity,

    @Column(name = "completion_percentage")
    val completionPercentage: Int = 0,

    @Column(name = "time_spent_minutes")
    val timeSpentMinutes: Int = 0,

    @Column(name = "last_studied")
    val lastStudied: LocalDateTime?,

    @Column(name = "next_review")
    val nextReview: LocalDateTime?,

    @Column(name = "spaced_repetition_level")
    val spacedRepetitionLevel: Int = 0,

    @Column(name = "notes", columnDefinition = "text")
    val notes: String?,

    @Column(name = "created_at")
    val createdAt: LocalDateTime = LocalDateTime.now(),

    @Column(name = "updated_at")
    val updatedAt: LocalDateTime = LocalDateTime.now(),
) 
