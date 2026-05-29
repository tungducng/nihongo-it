package io.github.ndtung723.nihongoit.learningservice.controller

import io.github.ndtung723.nihongoit.common.exception.BusinessException
import io.github.ndtung723.nihongoit.learningservice.dto.CommentDto
import io.github.ndtung723.nihongoit.learningservice.dto.CommentPageDto
import io.github.ndtung723.nihongoit.learningservice.dto.CreateCommentRequest
import io.github.ndtung723.nihongoit.learningservice.dto.LikeResult
import io.github.ndtung723.nihongoit.learningservice.dto.UpdateCommentRequest
import io.github.ndtung723.nihongoit.learningservice.service.CommentSort
import io.github.ndtung723.nihongoit.learningservice.service.VocabCommentService
import io.github.ndtung723.nihongoit.learningservice.util.UserAuthUtil
import io.swagger.v3.oas.annotations.Operation
import io.swagger.v3.oas.annotations.tags.Tag
import jakarta.servlet.http.HttpServletRequest
import jakarta.validation.Valid
import org.springframework.http.MediaType
import org.springframework.http.ResponseEntity
import org.springframework.security.access.prepost.PreAuthorize
import org.springframework.web.bind.annotation.DeleteMapping
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.PutMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RequestParam
import org.springframework.web.bind.annotation.RestController
import java.util.UUID

/**
 * Vocabulary comment thread endpoints. All require an authenticated user
 * (USER or ADMIN role); the gateway injects identity headers, this layer
 * trusts them.
 */
@RestController
@RequestMapping(produces = [MediaType.APPLICATION_JSON_VALUE])
@Tag(name = "Vocabulary Comments", description = "Threaded comments scoped per vocabulary entry")
class VocabCommentController(
    private val service: VocabCommentService,
    private val auth: UserAuthUtil,
) {
    @GetMapping("/api/v1/learning/vocabulary/{vocabId}/comments")
    @PreAuthorize("hasAnyRole('USER', 'ADMIN')")
    @Operation(summary = "List top-level comments for a vocab, paginated + sortable")
    fun listForVocab(
        @PathVariable vocabId: UUID,
        @RequestParam(defaultValue = "newest") sort: String,
        @RequestParam(defaultValue = "0") page: Int,
        @RequestParam(defaultValue = "10") size: Int,
    ): CommentPageDto =
        service.listForVocab(
            vocabId = vocabId,
            sort = CommentSort.parse(sort),
            page = page,
            size = size.coerceIn(1, 50),
            currentUserId = auth.getCurrentUserId(),
            isAdmin = auth.getCurrentRoleId() == 1,
        )

    @GetMapping("/api/v1/learning/comments/{commentId}/replies")
    @PreAuthorize("hasAnyRole('USER', 'ADMIN')")
    @Operation(summary = "List replies under a top-level comment (lazy-loaded)")
    fun listReplies(
        @PathVariable commentId: UUID,
        @RequestParam(defaultValue = "0") page: Int,
        @RequestParam(defaultValue = "5") size: Int,
    ): CommentPageDto =
        service.listReplies(
            parentId = commentId,
            page = page,
            size = size.coerceIn(1, 50),
            currentUserId = auth.getCurrentUserId(),
            isAdmin = auth.getCurrentRoleId() == 1,
        )

    @PostMapping("/api/v1/learning/vocabulary/{vocabId}/comments")
    @PreAuthorize("hasAnyRole('USER', 'ADMIN')")
    @Operation(summary = "Create a new comment or reply")
    fun create(
        @PathVariable vocabId: UUID,
        @Valid @RequestBody request: CreateCommentRequest,
        servletRequest: HttpServletRequest,
    ): CommentDto {
        val userId = auth.getCurrentUserId() ?: throw BusinessException("Chưa đăng nhập")
        val fullName =
            servletRequest.getHeader("X-User-Full-Name")?.takeIf { it.isNotBlank() }
                ?: auth.getCurrentEmail().orEmpty()
        return service.create(
            vocabId = vocabId,
            request = request,
            currentUserId = userId,
            currentUserFullName = fullName,
            isAdmin = auth.getCurrentRoleId() == 1,
        )
    }

    @PutMapping("/api/v1/learning/comments/{commentId}")
    @PreAuthorize("hasAnyRole('USER', 'ADMIN')")
    @Operation(summary = "Edit own comment (admin can edit any)")
    fun update(
        @PathVariable commentId: UUID,
        @Valid @RequestBody request: UpdateCommentRequest,
    ): CommentDto {
        val userId = auth.getCurrentUserId() ?: throw BusinessException("Chưa đăng nhập")
        return service.update(
            commentId = commentId,
            request = request,
            currentUserId = userId,
            isAdmin = auth.getCurrentRoleId() == 1,
        )
    }

    @DeleteMapping("/api/v1/learning/comments/{commentId}")
    @PreAuthorize("hasAnyRole('USER', 'ADMIN')")
    @Operation(summary = "Soft-delete own comment (admin can delete any)")
    fun delete(
        @PathVariable commentId: UUID,
    ): ResponseEntity<Void> {
        val userId = auth.getCurrentUserId() ?: throw BusinessException("Chưa đăng nhập")
        service.softDelete(commentId, userId, auth.getCurrentRoleId() == 1)
        return ResponseEntity.noContent().build()
    }

    @PostMapping("/api/v1/learning/comments/{commentId}/like")
    @PreAuthorize("hasAnyRole('USER', 'ADMIN')")
    @Operation(summary = "Add a like to a comment (idempotent)")
    fun like(
        @PathVariable commentId: UUID,
    ): LikeResult {
        val userId = auth.getCurrentUserId() ?: throw BusinessException("Chưa đăng nhập")
        return service.like(commentId, userId)
    }

    @DeleteMapping("/api/v1/learning/comments/{commentId}/like")
    @PreAuthorize("hasAnyRole('USER', 'ADMIN')")
    @Operation(summary = "Remove a like from a comment (idempotent)")
    fun unlike(
        @PathVariable commentId: UUID,
    ): LikeResult {
        val userId = auth.getCurrentUserId() ?: throw BusinessException("Chưa đăng nhập")
        return service.unlike(commentId, userId)
    }
}
