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
@Table(name = "user_progress")
data class UserProgressEntity(
    @Id
    @GeneratedValue(generator = "UUID")
    @Column(name = "progress_id", updatable = false, nullable = false)
    val progressId: UUID? = null,

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id")
    val user: UserEntity,

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "lesson_id")
    val lesson: LessonEntity,

    @Column(name = "completion_status", nullable = false)
    val completionStatus: Int, // 0: Not Started, 1: In Progress, 2: Completed

    @Column(name = "score")
    val score: Int?
)