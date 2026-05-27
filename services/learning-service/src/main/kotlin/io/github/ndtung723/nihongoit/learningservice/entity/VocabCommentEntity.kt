package io.github.ndtung723.nihongoit.learningservice.entity

import io.github.ndtung723.nihongoit.common.entity.AbstractAuditEntity
import jakarta.persistence.Column
import jakarta.persistence.Entity
import jakarta.persistence.GeneratedValue
import jakarta.persistence.Id
import jakarta.persistence.Table
import java.time.LocalDateTime
import java.util.UUID

/**
 * Comment thread node attached to a vocabulary entry. Top-level comments have
 * `parentCommentId = null`; replies point at their top-level parent. The model
 * enforces a single layer of replies (depth ≤ 1) at the service layer — this
 * entity itself accepts any parent, the rule lives in [VocabCommentService].
 *
 * `likeCount` / `replyCount` are denormalised: services UPDATE them atomically
 * on like/unlike + insert/soft-delete so listing pages never need a COUNT(*)
 * grouped subquery. See V9__vocab_comments.sql for the supporting indexes.
 */
@Entity
@Table(name = "vocab_comments")
data class VocabCommentEntity(
    @Id
    @GeneratedValue(generator = "UUID")
    @Column(name = "comment_id", updatable = false, nullable = false)
    val commentId: UUID? = null,
    @Column(name = "vocab_id", nullable = false)
    val vocabId: UUID,
    @Column(name = "parent_comment_id")
    val parentCommentId: UUID? = null,
    @Column(name = "user_id", nullable = false)
    val userId: UUID,
    @Column(name = "user_full_name", nullable = false, length = 100)
    val userFullName: String,
    @Column(name = "content", columnDefinition = "text", nullable = false)
    var content: String,
    @Column(name = "like_count", nullable = false)
    var likeCount: Int = 0,
    @Column(name = "reply_count", nullable = false)
    var replyCount: Int = 0,
    @Column(name = "deleted_at")
    var deletedAt: LocalDateTime? = null,
) : AbstractAuditEntity() {
    fun isDeleted(): Boolean = deletedAt != null
}
