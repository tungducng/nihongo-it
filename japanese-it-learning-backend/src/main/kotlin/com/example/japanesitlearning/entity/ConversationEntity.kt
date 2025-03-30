package com.example.japanesitlearning.entity

import jakarta.persistence.Column
import jakarta.persistence.Entity
import jakarta.persistence.GeneratedValue
import jakarta.persistence.Id
import jakarta.persistence.Table
import java.util.*

@Entity
@Table(name = "conversations")
data class ConversationEntity(
    @Id
    @GeneratedValue(generator = "UUID")
    @Column(name = "conversation_id", updatable = false, nullable = false)
    val conversationId: UUID? = null,

    @Column(name = "topic", nullable = false)
    val topic: String,

    @Column(name = "japanese_text", nullable = false, columnDefinition = "TEXT")
    val japaneseText: String,

    @Column(name = "english_text", nullable = false, columnDefinition = "TEXT")
    val englishText: String,

    @Column(name = "audio_url")
    val audioUrl: String?,

    @Column(name = "target_jlpt_level", nullable = false)
    val targetJlptLevel: Int, // 5: N5, 4: N4, 3: N3

    @Column(name = "business_context", nullable = false)
    val businessContext: Boolean = false,

    @Column(name = "tags")
    val tags: String?,

    @Column(name = "vocabulary_focus", columnDefinition = "TEXT")
    val vocabularyFocus: String?
)