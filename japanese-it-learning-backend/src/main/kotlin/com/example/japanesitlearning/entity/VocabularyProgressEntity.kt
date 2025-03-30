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
@Table(name = "vocabulary_progress")
data class VocabularyProgressEntity(
    @Id
    @GeneratedValue(generator = "UUID")
    val progressId: UUID? = null,

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id")
    val user: UserEntity,

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "vocabulary_id")
    val vocabulary: VocabularyEntity,

    @Column(name = "mastery_level", nullable = false)
    val masteryLevel: Int = 0, // 0-5 scale

    @Column(name = "last_review_date")
    val lastReviewDate: Date? = null,

    @Column(name = "next_review_date")
    val nextReviewDate: Date? = null,

    @Column(name = "review_count", nullable = false)
    val reviewCount: Int = 0,

    @Column(name = "ease_factor", nullable = false)
    val easeFactor: Float = 2.5f
)