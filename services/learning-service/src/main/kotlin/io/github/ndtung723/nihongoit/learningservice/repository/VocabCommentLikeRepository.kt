package io.github.ndtung723.nihongoit.learningservice.repository

import io.github.ndtung723.nihongoit.learningservice.entity.VocabCommentLikeEntity
import io.github.ndtung723.nihongoit.learningservice.entity.VocabCommentLikeId
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.jpa.repository.Query
import org.springframework.data.repository.query.Param
import org.springframework.stereotype.Repository
import java.util.UUID

@Repository
interface VocabCommentLikeRepository : JpaRepository<VocabCommentLikeEntity, VocabCommentLikeId> {
    fun existsByCommentIdAndUserId(commentId: UUID, userId: UUID): Boolean

    fun deleteByCommentIdAndUserId(commentId: UUID, userId: UUID): Long

    /**
     * Batched "has the current user liked any of these comments" lookup — used
     * when annotating a page of comments. Returns the subset of input IDs that
     * the user has liked.
     */
    @Query(
        """
        SELECT l.commentId FROM VocabCommentLikeEntity l
        WHERE l.userId = :userId AND l.commentId IN :commentIds
        """,
    )
    fun findLikedCommentIds(
        @Param("userId") userId: UUID,
        @Param("commentIds") commentIds: Collection<UUID>,
    ): List<UUID>
}
