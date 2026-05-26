package io.github.ndtung723.nihongoit.learningservice.entity

import jakarta.persistence.*
import java.io.Serializable
import java.util.UUID

/**
 * Join entity for the saved_vocabulary table — many-to-many between users
 * (cross-service UUID) and vocabulary. Replaces the previous JPA ManyToMany
 * association that referenced a local UserEntity.
 */
@Entity
@Table(name = "saved_vocabulary")
@IdClass(SavedVocabularyId::class)
data class SavedVocabularyEntity(
    @Id
    @Column(name = "vocab_id", nullable = false)
    val vocabId: UUID,
    @Id
    @Column(name = "user_id", nullable = false)
    val userId: UUID,
)

data class SavedVocabularyId(
    val vocabId: UUID = UUID(0, 0),
    val userId: UUID = UUID(0, 0),
) : Serializable {
    companion object {
        private const val serialVersionUID: Long = 1L
    }
}
