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

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "created_by")
    val createdBy: UserEntity,

    @Column(name = "created_at")
    val createdAt: LocalDateTime = LocalDateTime.now(),

    @Column(name = "updated_at")
    val updatedAt: LocalDateTime = LocalDateTime.now()
)


