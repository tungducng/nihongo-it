package io.github.ndtung723.nihongoit.learningservice.service

import io.github.ndtung723.nihongoit.common.exception.BusinessException
import io.github.ndtung723.nihongoit.learningservice.dto.CommentDto
import io.github.ndtung723.nihongoit.learningservice.dto.CommentPageDto
import io.github.ndtung723.nihongoit.learningservice.dto.CreateCommentRequest
import io.github.ndtung723.nihongoit.learningservice.dto.LikeResult
import io.github.ndtung723.nihongoit.learningservice.dto.UpdateCommentRequest
import io.github.ndtung723.nihongoit.learningservice.entity.VocabCommentEntity
import io.github.ndtung723.nihongoit.learningservice.entity.VocabCommentLikeEntity
import io.github.ndtung723.nihongoit.learningservice.repository.VocabCommentLikeRepository
import io.github.ndtung723.nihongoit.learningservice.repository.VocabCommentRepository
import io.github.ndtung723.nihongoit.learningservice.repository.VocabularyRepository
import org.springframework.dao.DataIntegrityViolationException
import org.springframework.data.domain.PageRequest
import org.springframework.data.domain.Sort
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import java.time.LocalDateTime
import java.util.UUID

enum class CommentSort {
    NEWEST,
    TOP,
    ;

    fun toPageable(page: Int, size: Int): PageRequest =
        when (this) {
            NEWEST -> PageRequest.of(page, size, Sort.by(Sort.Direction.DESC, "createdAt"))
            TOP ->
                PageRequest.of(
                    page,
                    size,
                    Sort.by(Sort.Direction.DESC, "likeCount").and(Sort.by(Sort.Direction.DESC, "createdAt")),
                )
        }

    companion object {
        fun parse(s: String?): CommentSort =
            when (s?.lowercase()) {
                "top", "popular" -> TOP
                else -> NEWEST
            }
    }
}

@Service
class VocabCommentService(
    private val commentRepo: VocabCommentRepository,
    private val likeRepo: VocabCommentLikeRepository,
    private val vocabRepo: VocabularyRepository,
) {
    @Transactional(readOnly = true)
    fun listForVocab(
        vocabId: UUID,
        sort: CommentSort,
        page: Int,
        size: Int,
        currentUserId: UUID?,
        isAdmin: Boolean,
    ): CommentPageDto {
        if (!vocabRepo.existsById(vocabId)) {
            throw BusinessException("Không tìm thấy từ vựng")
        }
        val pageable = sort.toPageable(page, size)
        val pageResult = commentRepo.findTopLevelByVocab(vocabId, pageable)
        val likedIds = currentUserId?.let { uid ->
            val ids = pageResult.content.mapNotNull { it.commentId }
            if (ids.isEmpty()) emptySet() else likeRepo.findLikedCommentIds(uid, ids).toSet()
        } ?: emptySet()
        val dtos = pageResult.content.map { entity ->
            CommentDto.from(entity, likedIds.contains(entity.commentId), currentUserId, isAdmin)
        }
        return CommentPageDto(
            content = dtos,
            page = page,
            size = size,
            totalElements = pageResult.totalElements,
            totalPages = pageResult.totalPages,
            totalComments = commentRepo.countActiveByVocab(vocabId),
        )
    }

    @Transactional(readOnly = true)
    fun listReplies(
        parentId: UUID,
        page: Int,
        size: Int,
        currentUserId: UUID?,
        isAdmin: Boolean,
    ): CommentPageDto {
        commentRepo.findById(parentId).orElseThrow { BusinessException("Không tìm thấy bình luận") }
        val pageable = PageRequest.of(page, size)
        val pageResult = commentRepo.findRepliesByParent(parentId, pageable)
        val likedIds = currentUserId?.let { uid ->
            val ids = pageResult.content.mapNotNull { it.commentId }
            if (ids.isEmpty()) emptySet() else likeRepo.findLikedCommentIds(uid, ids).toSet()
        } ?: emptySet()
        val dtos = pageResult.content.map { entity ->
            CommentDto.from(entity, likedIds.contains(entity.commentId), currentUserId, isAdmin)
        }
        return CommentPageDto(
            content = dtos,
            page = page,
            size = size,
            totalElements = pageResult.totalElements,
            totalPages = pageResult.totalPages,
        )
    }

    @Transactional
    fun create(
        vocabId: UUID,
        request: CreateCommentRequest,
        currentUserId: UUID,
        currentUserFullName: String,
        isAdmin: Boolean,
    ): CommentDto {
        val content = request.content.trim()
        if (content.isEmpty()) throw BusinessException("Bình luận không được để trống")
        if (!vocabRepo.existsById(vocabId)) throw BusinessException("Không tìm thấy từ vựng")

        // Validate parent: same vocab, top-level (depth limit = 1)
        request.parentCommentId?.let { parentId ->
            val parent = commentRepo.findById(parentId)
                .orElseThrow { BusinessException("Không tìm thấy bình luận cha") }
            if (parent.vocabId != vocabId) {
                throw BusinessException("Bình luận cha không thuộc từ vựng này")
            }
            if (parent.parentCommentId != null) {
                throw BusinessException("Chỉ trả lời được bình luận gốc — depth tối đa 1")
            }
            if (parent.isDeleted()) {
                throw BusinessException("Bình luận gốc đã bị xoá")
            }
        }

        val entity = VocabCommentEntity(
            vocabId = vocabId,
            parentCommentId = request.parentCommentId,
            userId = currentUserId,
            userFullName = currentUserFullName.ifBlank { "Người dùng ẩn danh" },
            content = content,
        )
        val saved = commentRepo.saveAndFlush(entity)

        // Bump parent reply count if this is a reply
        request.parentCommentId?.let { commentRepo.incrementReplyCount(it) }

        return CommentDto.from(saved, liked = false, currentUserId = currentUserId, isAdmin = isAdmin)
    }

    @Transactional
    fun update(
        commentId: UUID,
        request: UpdateCommentRequest,
        currentUserId: UUID,
        isAdmin: Boolean,
    ): CommentDto {
        val entity = commentRepo.findById(commentId)
            .orElseThrow { BusinessException("Không tìm thấy bình luận") }
        if (entity.userId != currentUserId && !isAdmin) {
            throw BusinessException("Không có quyền sửa bình luận này")
        }
        if (entity.isDeleted()) throw BusinessException("Bình luận đã bị xoá")

        val content = request.content.trim()
        if (content.isEmpty()) throw BusinessException("Bình luận không được để trống")
        entity.content = content
        val saved = commentRepo.save(entity)

        val liked = likeRepo.existsByCommentIdAndUserId(commentId, currentUserId)
        return CommentDto.from(saved, liked = liked, currentUserId = currentUserId, isAdmin = isAdmin)
    }

    @Transactional
    fun softDelete(commentId: UUID, currentUserId: UUID, isAdmin: Boolean) {
        val entity = commentRepo.findById(commentId)
            .orElseThrow { BusinessException("Không tìm thấy bình luận") }
        if (entity.userId != currentUserId && !isAdmin) {
            throw BusinessException("Không có quyền xoá bình luận này")
        }
        if (entity.isDeleted()) return // idempotent

        entity.deletedAt = LocalDateTime.now()
        commentRepo.save(entity)
        entity.parentCommentId?.let { commentRepo.decrementReplyCount(it) }
    }

    @Transactional
    fun like(commentId: UUID, currentUserId: UUID): LikeResult {
        val entity = commentRepo.findById(commentId)
            .orElseThrow { BusinessException("Không tìm thấy bình luận") }
        if (entity.isDeleted()) throw BusinessException("Không thể thả tim bình luận đã xoá")

        if (likeRepo.existsByCommentIdAndUserId(commentId, currentUserId)) {
            return LikeResult(likeCount = entity.likeCount, liked = true)
        }
        try {
            likeRepo.save(VocabCommentLikeEntity(commentId = commentId, userId = currentUserId))
            commentRepo.incrementLikeCount(commentId)
        } catch (_: DataIntegrityViolationException) {
            // Race — another concurrent like won. Treat as success.
        }
        val fresh = commentRepo.findById(commentId).get()
        return LikeResult(likeCount = fresh.likeCount, liked = true)
    }

    @Transactional
    fun unlike(commentId: UUID, currentUserId: UUID): LikeResult {
        val deleted = likeRepo.deleteByCommentIdAndUserId(commentId, currentUserId)
        if (deleted > 0) {
            commentRepo.decrementLikeCount(commentId)
        }
        val fresh = commentRepo.findById(commentId)
            .orElseThrow { BusinessException("Không tìm thấy bình luận") }
        return LikeResult(likeCount = fresh.likeCount, liked = false)
    }
}
