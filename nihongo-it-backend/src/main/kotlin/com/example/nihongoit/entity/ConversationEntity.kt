package com.example.nihongoit.entity

import jakarta.persistence.*
import java.time.LocalDateTime
import java.util.UUID

@Entity
@Table(name = "conversations")
data class ConversationEntity(
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @Column(name = "conversation_id", nullable = false)
    val conversationId: UUID = UUID.randomUUID(),

    @Column(name = "title", nullable = false)
    val title: String,

    @Column(name = "description", columnDefinition = "text")
    val description: String,

    @Enumerated(EnumType.STRING)
    @Column(name = "jlpt_level", nullable = false)
    val jlptLevel: JlptLevel,

    @Column(name = "it_context", nullable = false)
    val itContext: String, // e.g., "Job Interview", "Code Review", "Team Meeting"

    @Column(name = "main_grammar_points", columnDefinition = "text")
    val mainGrammarPoints: String? = null,

    @Column(name = "main_vocabulary_theme")
    val mainVocabularyTheme: String? = null,

    @Column(name = "audio_url")
    val audioUrl: String? = null,

    @Column(name = "transcript_url")
    val transcriptUrl: String? = null,

    @Column(name = "scenario_setup", columnDefinition = "text")
    val scenarioSetup: String,

    @OneToMany(mappedBy = "conversation", cascade = [CascadeType.ALL], orphanRemoval = true)
    val lines: MutableList<ConversationLineEntity> = mutableListOf(),

    @Column(name = "has_quiz")
    val hasQuiz: Boolean = false,

    @Column(name = "is_active")
    val isActive: Boolean = true,

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "created_by")
    val createdBy: UserEntity,

    @Column(name = "created_at")
    val createdAt: LocalDateTime = LocalDateTime.now(),

    @Column(name = "updated_at")
    val updatedAt: LocalDateTime = LocalDateTime.now()
)

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

    @Column(name = "english_translation", columnDefinition = "text")
    val englishTranslation: String,

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
