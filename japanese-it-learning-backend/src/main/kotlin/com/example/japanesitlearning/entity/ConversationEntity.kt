package com.example.japanesitlearning.entity

import jakarta.persistence.*
import java.time.LocalDateTime
import java.util.*

@Entity
@Table(name = "conversations")
data class ConversationEntity(
    @Id
    @GeneratedValue
    @Column(name = "conversation_id")
    val conversationId: UUID = UUID.randomUUID(),

    @Column(name = "title", length = 200)
    val title: String,

    @Column(name = "description", columnDefinition = "text")
    val description: String,

    @Column(name = "level")
    val level: Int, // JLPT level (N5-N1)

    @Column(name = "it_context", columnDefinition = "text")
    val itContext: String?,

    @Column(name = "created_at")
    val createdAt: LocalDateTime = LocalDateTime.now(),

    @Column(name = "updated_at")
    val updatedAt: LocalDateTime = LocalDateTime.now(),
)

@Entity
@Table(name = "conversation_dialogues")
data class ConversationDialogueEntity(
    @Id
    @GeneratedValue
    @Column(name = "dialogue_id")
    val dialogueId: UUID = UUID.randomUUID(),

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "conversation_id")
    val conversation: ConversationEntity,

    @Column(name = "speaker")
    @Enumerated(EnumType.STRING)
    val speaker: Speaker,

    @Column(name = "japanese_text", columnDefinition = "text")
    val japaneseText: String,

    @Column(name = "english_translation", columnDefinition = "text")
    val englishTranslation: String,

    @Column(name = "order_index")
    val orderIndex: Int,

    @Column(name = "audio_url")
    val audioUrl: String?,

    @Column(name = "created_at")
    val createdAt: LocalDateTime = LocalDateTime.now(),

    @Column(name = "updated_at")
    val updatedAt: LocalDateTime = LocalDateTime.now(),
)

enum class Speaker {
    USER,
    AI,
    NATIVE_SPEAKER,
}
