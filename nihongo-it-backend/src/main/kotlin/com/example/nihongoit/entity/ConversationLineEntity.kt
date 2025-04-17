package com.example.nihongoit.entity

import jakarta.persistence.Column
import jakarta.persistence.Entity
import jakarta.persistence.FetchType
import jakarta.persistence.GeneratedValue
import jakarta.persistence.GenerationType
import jakarta.persistence.Id
import jakarta.persistence.JoinColumn
import jakarta.persistence.ManyToOne
import jakarta.persistence.Table
import java.util.*

@Entity
@Table(name = "conversation_lines")
data class ConversationLineEntity(
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @Column(name = "line_id")
    val lineId: UUID = UUID.randomUUID(),

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "conversation_id", nullable = false)
    val conversation: ConversationEntity,

    @Column(name = "speaker", nullable = false)
    val speaker: String,

    @Column(name = "japanese_text", nullable = false, columnDefinition = "text")
    val japaneseText: String,

    @Column(name = "vietnamese_translation", columnDefinition = "text")
    val vietnameseTranslation: String,

    @Column(name = "romaji", columnDefinition = "text")
    val romaji: String? = null,

    @Column(name = "notes", columnDefinition = "text")
    val notes: String? = null,

    @Column(name = "important_vocab", columnDefinition = "text")
    val importantVocab: String? = null,

    @Column(name = "line_audio_start_time")
    val lineAudioStartTime: Double? = null, // time in seconds

    @Column(name = "line_audio_end_time")
    val lineAudioEndTime: Double? = null, // time in seconds

    @Column(name = "order_index", nullable = false)
    val orderIndex: Int = 0
)