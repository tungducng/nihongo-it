package com.example.nihongoit.entity

import jakarta.persistence.*
import java.time.LocalDateTime
import java.util.UUID

@Entity
@Table(name = "grammar_points")
data class GrammarEntity(
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @Column(name = "grammar_id")
    val grammarId: UUID = UUID.randomUUID(),
    
    @Column(name = "pattern", nullable = false)
    val pattern: String,
    
    @Column(name = "meaning", nullable = false)
    val meaning: String,
    
    @Column(name = "explanation", columnDefinition = "text")
    val explanation: String,
    
    @Column(name = "formation", columnDefinition = "text")
    val formation: String,
    
    @Enumerated(EnumType.STRING)
    @Column(name = "jlpt_level", nullable = false)
    val jlptLevel: JlptLevel,
    
    @Column(name = "it_context_explanation", columnDefinition = "text")
    val itContextExplanation: String,
    
    @OneToMany(mappedBy = "grammar", cascade = [CascadeType.ALL], orphanRemoval = true)
    val examples: MutableList<GrammarExampleEntity> = mutableListOf(),
    
    @Column(name = "related_grammar")
    val relatedGrammar: String? = null,
    
    @Column(name = "notes", columnDefinition = "text")
    val notes: String? = null,
    
    @Column(name = "difficulty_level")
    val difficultyLevel: Int = 1, // 1=Beginner, 2=Intermediate, 3=Advanced
    
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "created_by")
    val createdBy: UserEntity,
    
    @Column(name = "created_at")
    val createdAt: LocalDateTime = LocalDateTime.now(),
    
    @Column(name = "updated_at")
    val updatedAt: LocalDateTime = LocalDateTime.now()
)

@Entity
@Table(name = "grammar_examples")
data class GrammarExampleEntity(
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @Column(name = "example_id")
    val exampleId: UUID = UUID.randomUUID(),
    
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "grammar_id", nullable = false)
    val grammar: GrammarEntity,
    
    @Column(name = "japanese_text", nullable = false)
    val japaneseText: String,
    
    @Column(name = "english_translation", nullable = false)
    val englishTranslation: String,
    
    @Column(name = "romaji")
    val romaji: String? = null,
    
    @Column(name = "audio_url")
    val audioUrl: String? = null,
    
    @Column(name = "it_specialty")
    val itSpecialty: String? = null, // Programming, Database, Networking, etc.
    
    @Column(name = "notes")
    val notes: String? = null,
    
    @Column(name = "order_index")
    val orderIndex: Int = 0
) 