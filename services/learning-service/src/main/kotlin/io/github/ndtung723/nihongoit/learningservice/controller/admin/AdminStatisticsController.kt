package io.github.ndtung723.nihongoit.learningservice.controller.admin

import io.github.ndtung723.nihongoit.learningservice.dto.StudyStatisticsDto
import io.github.ndtung723.nihongoit.learningservice.service.FlashcardAdminService
import io.github.ndtung723.nihongoit.learningservice.service.FlashcardStatisticsService
import io.github.ndtung723.nihongoit.learningservice.service.UserProgressService
import io.swagger.v3.oas.annotations.Operation
import io.swagger.v3.oas.annotations.security.SecurityRequirement
import io.swagger.v3.oas.annotations.tags.Tag
import org.slf4j.LoggerFactory
import org.springframework.http.MediaType
import org.springframework.http.ResponseEntity
import org.springframework.security.access.prepost.PreAuthorize
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController
import java.time.format.DateTimeFormatter
import java.util.UUID

/**
 * Per-user and aggregate flashcard statistics. Caller (FE admin) is expected
 * to provide the user list / identity data from `/api/v1/user/admin/users*`
 * and use these endpoints to enrich each row with learning-domain stats.
 */
@RestController
@RequestMapping("/api/v1/learning/admin/statistics")
@Tag(name = "Admin Statistics (Learning)", description = "Per-user + aggregate flashcard stats")
@PreAuthorize("hasRole('ADMIN')")
class AdminStatisticsController(
    private val flashcardStatisticsService: FlashcardStatisticsService,
    private val flashcardAdminService: FlashcardAdminService,
    private val userProgressService: UserProgressService,
) {
    private val logger = LoggerFactory.getLogger(AdminStatisticsController::class.java)
    private val dateTimeFormatter: DateTimeFormatter = DateTimeFormatter.ISO_DATE_TIME

    @GetMapping("/users/{userId}", produces = [MediaType.APPLICATION_JSON_VALUE])
    @Operation(
        summary = "Per-user flashcard statistics + learning progress",
        security = [SecurityRequirement(name = "bearerAuth")],
    )
    fun getUserStatisticsById(
        @PathVariable userId: UUID,
    ): ResponseEntity<Any> {
        logger.info("Fetching learning stats for user: $userId")

        val flashcardStats = flashcardStatisticsService.getUserFlashcardStatistics(userId)
        val lastReview = flashcardStatisticsService.getLastReviewDate(userId)
        val reviewHistory = flashcardStatisticsService.getUserReviewHistory(userId, 30)
        val progress = userProgressService.getOrCreate(userId)
        val lastActive =
            lastReview?.format(dateTimeFormatter)
                ?: progress.lastStudyDate?.format(dateTimeFormatter)
                ?: ""

        val payload =
            mapOf(
                "userId" to userId,
                "streakCount" to progress.streakCount,
                "points" to progress.points,
                "dailyGoalMinutes" to progress.dailyGoalMinutes,
                "summary" to flashcardStats.summary,
                "cardsByState" to flashcardStats.cardsByState,
                "cardsByJlptLevel" to flashcardStats.cardsByJlptLevel,
                "dailyReviews" to flashcardStats.dailyReviews,
                "retentionRateByDay" to flashcardStats.retentionRateByDay,
                "memoryStrengthDistribution" to flashcardStats.memoryStrengthDistribution,
                "cardsDueByDay" to flashcardStats.cardsDueByDay,
                "lastActive" to lastActive,
                "progress" to calculateUserProgress(flashcardStats),
                "reviewHistory" to reviewHistory,
            )

        return ResponseEntity.ok(payload)
    }

    @GetMapping("/overview", produces = [MediaType.APPLICATION_JSON_VALUE])
    @Operation(
        summary = "Aggregate flashcard stats for the admin dashboard",
        security = [SecurityRequirement(name = "bearerAuth")],
    )
    fun getStatisticsOverview(): ResponseEntity<Any> {
        logger.info("Fetching learning-domain statistics overview")

        val totalFlashcards = flashcardAdminService.getTotalFlashcardsCount()
        val averageRetentionRate = flashcardAdminService.getAverageRetentionRate()

        val overview =
            mapOf(
                "totalFlashcards" to totalFlashcards,
                "averageRetentionRate" to averageRetentionRate,
            )

        return ResponseEntity.ok(overview)
    }

    private fun calculateUserProgress(stats: StudyStatisticsDto): Int {
        val totalCards = stats.summary.totalCards
        if (totalCards == 0) return 0
        val reviewCards = stats.cardsByState["review"] ?: 0
        val graduatedCards = stats.cardsByState["graduated"] ?: 0
        return ((reviewCards + graduatedCards) * 100 / totalCards)
    }
}
