package io.github.ndtung723.nihongoit.learningservice.dto

import io.github.ndtung723.nihongoit.learningservice.entity.VocabCommentEntity
import java.time.LocalDateTime
import java.util.UUID

data class CommentUserDto(
    val userId: UUID,
    val fullName: String,
    val initials: String,
)

data class CommentDto(
    val commentId: UUID,
    val vocabId: UUID,
    val parentCommentId: UUID?,
    val user: CommentUserDto,
    val content: String,
    val likeCount: Int,
    val replyCount: Int,
    val liked: Boolean,
    val edited: Boolean,
    val deleted: Boolean,
    val canEdit: Boolean,
    val createdAt: LocalDateTime,
    val updatedAt: LocalDateTime,
) {
    companion object {
        private const val DELETED_PLACEHOLDER = "[Bình luận đã xoá]"

        fun from(
            entity: VocabCommentEntity,
            liked: Boolean,
            currentUserId: UUID?,
            isAdmin: Boolean,
        ): CommentDto {
            val deleted = entity.isDeleted()
            val edited = entity.updatedAt != null && entity.createdAt != null && entity.updatedAt != entity.createdAt
            val ownership = currentUserId != null && currentUserId == entity.userId
            return CommentDto(
                commentId = entity.commentId!!,
                vocabId = entity.vocabId,
                parentCommentId = entity.parentCommentId,
                user = CommentUserDto(
                    userId = entity.userId,
                    fullName = entity.userFullName,
                    initials = entity.userFullName.computeInitials(),
                ),
                content = if (deleted) DELETED_PLACEHOLDER else entity.content,
                likeCount = entity.likeCount,
                replyCount = entity.replyCount,
                liked = liked,
                edited = edited && !deleted,
                deleted = deleted,
                canEdit = !deleted && (ownership || isAdmin),
                createdAt = entity.createdAt ?: LocalDateTime.now(),
                updatedAt = entity.updatedAt ?: LocalDateTime.now(),
            )
        }

        private fun String.computeInitials(): String {
            val parts = trim().split(Regex("\\s+")).filter { it.isNotEmpty() }
            if (parts.isEmpty()) return "?"
            return parts.take(2).joinToString("") { it.first().uppercaseChar().toString() }
        }
    }
}

data class CommentPageDto(
    val content: List<CommentDto>,
    val page: Int,
    val size: Int,
    val totalElements: Long,
    val totalPages: Int,
    val totalComments: Long? = null,
)
