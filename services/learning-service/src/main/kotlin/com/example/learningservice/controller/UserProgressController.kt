package com.example.learningservice.controller

import com.example.common.exception.BusinessException
import com.example.learningservice.dto.UserProgressDto
import com.example.learningservice.service.UserProgressService
import com.example.learningservice.util.UserAuthUtil
import io.swagger.v3.oas.annotations.Operation
import io.swagger.v3.oas.annotations.tags.Tag
import org.springframework.http.MediaType
import org.springframework.security.access.prepost.PreAuthorize
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController
import java.util.UUID

/**
 * Read-only endpoints exposing the caller's learning progress (streak, points,
 * daily goal). Separated from the FSRS-heavy FlashcardController because
 * `user_progress` is its own aggregate.
 */
@RestController
@RequestMapping("/api/v1/learning/users", produces = [MediaType.APPLICATION_JSON_VALUE])
@Tag(name = "User Progress", description = "Learning-domain per-user stats (streak, points)")
class UserProgressController(
    private val userProgressService: UserProgressService,
    private val userAuthUtil: UserAuthUtil,
) {
    @GetMapping("/me/progress")
    @PreAuthorize("hasAnyRole('USER', 'ADMIN')")
    @Operation(summary = "Get the current user's learning progress")
    fun myProgress(): UserProgressDto {
        val userId = userAuthUtil.getCurrentUserId() ?: throw BusinessException("User not authenticated")
        return UserProgressDto.fromEntity(userProgressService.getOrCreate(userId))
    }

    @GetMapping("/{userId}/progress")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Get any user's learning progress (admin)")
    fun progressByUserId(
        @PathVariable userId: UUID,
    ): UserProgressDto = UserProgressDto.fromEntity(userProgressService.getOrCreate(userId))
}
