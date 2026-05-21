package com.example.userservice.controller.admin

import com.example.userservice.repository.UserRepository
import io.swagger.v3.oas.annotations.Operation
import io.swagger.v3.oas.annotations.tags.Tag
import org.springframework.http.MediaType
import org.springframework.security.access.prepost.PreAuthorize
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RequestParam
import org.springframework.web.bind.annotation.RestController
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter

/**
 * Admin-only user aggregate stats. Lives in user-service so the queries run
 * against the canonical users table — no cross-service calls needed.
 *
 * Counterpart learning-domain stats (flashcards, vocab) live in
 * learning-service.AdminDashboardController. The frontend admin combines
 * results from both.
 */
@RestController
@RequestMapping("/api/v1/user/admin/users/stats", produces = [MediaType.APPLICATION_JSON_VALUE])
@PreAuthorize("hasRole('ADMIN')")
@Tag(name = "Admin — User Stats", description = "Aggregate stats over the users table")
class AdminUserStatsController(
    private val userRepository: UserRepository,
) {
    private val dateTimeFormatter: DateTimeFormatter = DateTimeFormatter.ISO_DATE_TIME

    @GetMapping("/count")
    @Operation(summary = "Total number of users in the system")
    fun count(): Map<String, Long> = mapOf("count" to userRepository.count())

    @GetMapping("/new-since")
    @Operation(summary = "Users created since the given timestamp (ISO-8601 LocalDateTime)")
    fun newSince(
        @RequestParam since: String,
    ): Map<String, Long> {
        val parsed = LocalDateTime.parse(since)
        return mapOf("count" to userRepository.countByCreatedAtAfter(parsed))
    }

    @GetMapping("/active-since")
    @Operation(summary = "Users who logged in since the given timestamp")
    fun activeSince(
        @RequestParam since: String,
    ): Map<String, Long> {
        val parsed = LocalDateTime.parse(since)
        return mapOf("count" to userRepository.countByLastLoginAfter(parsed))
    }

    @GetMapping("/by-level")
    @Operation(summary = "User count grouped by current JLPT level")
    fun byLevel(): Map<String, Int> =
        userRepository.countGroupByCurrentLevel().associate {
            val level = (it[0] as? Enum<*>)?.name ?: "Not specified"
            level to ((it[1] as? Long) ?: 0L).toInt()
        }

    @GetMapping("/by-jlpt-goal")
    @Operation(summary = "User count grouped by JLPT goal")
    fun byJlptGoal(): Map<String, Int> =
        userRepository.countGroupByJlptGoal().associate {
            val goal = (it[0] as? Enum<*>)?.name ?: "Not specified"
            goal to ((it[1] as? Long) ?: 0L).toInt()
        }

    @GetMapping("/recent-activities")
    @Operation(summary = "Recent user activity entries derived from last_login")
    fun recentActivities(
        @RequestParam(defaultValue = "10") limit: Int,
    ): List<Map<String, Any?>> {
        val users = userRepository.findTop10ByOrderByLastLoginDesc()
        val capped = users.take(limit.coerceIn(1, MAX_LIMIT))
        return capped.mapNotNull { user ->
            val lastLogin = user.lastLogin ?: return@mapNotNull null
            val createdAt = user.createdAt
            val action =
                when {
                    lastLogin.isAfter(LocalDateTime.now().minusHours(1)) -> "Đã đăng nhập"
                    createdAt != null && createdAt.isAfter(LocalDateTime.now().minusDays(1)) -> "Đã tạo tài khoản mới"
                    else -> "Đã truy cập hệ thống"
                }
            mapOf(
                "user" to user.email,
                "action" to action,
                "timestamp" to lastLogin.format(dateTimeFormatter),
            )
        }
    }

    companion object {
        private const val MAX_LIMIT = 100
    }
}
