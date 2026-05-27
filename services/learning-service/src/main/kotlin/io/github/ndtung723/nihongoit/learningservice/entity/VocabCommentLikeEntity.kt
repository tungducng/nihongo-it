package io.github.ndtung723.nihongoit.learningservice.entity

import jakarta.persistence.Column
import jakarta.persistence.Entity
import jakarta.persistence.Id
import jakarta.persistence.IdClass
import jakarta.persistence.Table
import java.io.Serializable
import java.time.LocalDateTime
import java.util.UUID

/**
 * Like join table — composite PK on (comment_id, user_id) makes "has user X
 * liked comment Y" an O(1) index lookup and prevents double-likes at the DB
 * level. The service is then free to use INSERT … ON CONFLICT DO NOTHING.
 */
@Entity
@Table(name = "vocab_comment_likes")
@IdClass(VocabCommentLikeId::class)
data class VocabCommentLikeEntity(
    @Id
    @Column(name = "comment_id", nullable = false)
    val commentId: UUID,
    @Id
    @Column(name = "user_id", nullable = false)
    val userId: UUID,
    @Column(name = "created_at", nullable = false)
    val createdAt: LocalDateTime = LocalDateTime.now(),
)

data class VocabCommentLikeId(
    val commentId: UUID = UUID(0, 0),
    val userId: UUID = UUID(0, 0),
) : Serializable {
    companion object {
        private const val serialVersionUID: Long = 1L
    }
}
