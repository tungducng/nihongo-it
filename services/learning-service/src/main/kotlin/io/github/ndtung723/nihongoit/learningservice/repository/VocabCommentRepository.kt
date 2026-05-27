package io.github.ndtung723.nihongoit.learningservice.repository

import io.github.ndtung723.nihongoit.learningservice.entity.VocabCommentEntity
import org.springframework.data.domain.Page
import org.springframework.data.domain.Pageable
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.jpa.repository.Modifying
import org.springframework.data.jpa.repository.Query
import org.springframework.data.repository.query.Param
import org.springframework.stereotype.Repository
import java.util.UUID

@Repository
interface VocabCommentRepository : JpaRepository<VocabCommentEntity, UUID> {
    /**
     * Top-level comments for a vocab, excluding soft-deleted, sorted by the
     * caller's chosen Pageable (newest or popular).
     */
    @Query(
        """
        SELECT c FROM VocabCommentEntity c
        WHERE c.vocabId = :vocabId
          AND c.parentCommentId IS NULL
          AND c.deletedAt IS NULL
        """,
    )
    fun findTopLevelByVocab(
        @Param("vocabId") vocabId: UUID,
        pageable: Pageable,
    ): Page<VocabCommentEntity>

    /**
     * Replies under a single top-level comment. Replies are ALWAYS ordered
     * oldest-first so the conversation reads naturally.
     */
    @Query(
        """
        SELECT c FROM VocabCommentEntity c
        WHERE c.parentCommentId = :parentId
          AND c.deletedAt IS NULL
        ORDER BY c.createdAt ASC
        """,
    )
    fun findRepliesByParent(
        @Param("parentId") parentId: UUID,
        pageable: Pageable,
    ): Page<VocabCommentEntity>

    /** Eyebrow badge total — top-level + replies, excluding soft-deleted. */
    @Query(
        "SELECT COUNT(c) FROM VocabCommentEntity c WHERE c.vocabId = :vocabId AND c.deletedAt IS NULL",
    )
    fun countActiveByVocab(
        @Param("vocabId") vocabId: UUID,
    ): Long

    @Modifying
    @Query("UPDATE VocabCommentEntity c SET c.replyCount = c.replyCount + 1 WHERE c.commentId = :id")
    fun incrementReplyCount(
        @Param("id") id: UUID,
    ): Int

    @Modifying
    @Query("UPDATE VocabCommentEntity c SET c.replyCount = c.replyCount - 1 WHERE c.commentId = :id AND c.replyCount > 0")
    fun decrementReplyCount(
        @Param("id") id: UUID,
    ): Int

    @Modifying
    @Query("UPDATE VocabCommentEntity c SET c.likeCount = c.likeCount + 1 WHERE c.commentId = :id")
    fun incrementLikeCount(
        @Param("id") id: UUID,
    ): Int

    @Modifying
    @Query("UPDATE VocabCommentEntity c SET c.likeCount = c.likeCount - 1 WHERE c.commentId = :id AND c.likeCount > 0")
    fun decrementLikeCount(
        @Param("id") id: UUID,
    ): Int
}
