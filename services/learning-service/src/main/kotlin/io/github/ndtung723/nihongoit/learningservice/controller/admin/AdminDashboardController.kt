package io.github.ndtung723.nihongoit.learningservice.controller.admin

import io.github.ndtung723.nihongoit.learningservice.service.CategoryService
import io.github.ndtung723.nihongoit.learningservice.service.FlashcardCrudService
import io.github.ndtung723.nihongoit.learningservice.service.TopicService
import io.github.ndtung723.nihongoit.learningservice.service.VocabularyService
import io.swagger.v3.oas.annotations.Operation
import io.swagger.v3.oas.annotations.security.SecurityRequirement
import io.swagger.v3.oas.annotations.tags.Tag
import org.slf4j.LoggerFactory
import org.springframework.http.MediaType
import org.springframework.http.ResponseEntity
import org.springframework.security.access.prepost.PreAuthorize
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController
import java.time.LocalDateTime

/**
 * Learning-domain dashboard stats — vocabulary, topic, category, flashcards.
 *
 * User-related aggregates (userCount, newUsers, activeUsers, recentActivities)
 * are now served by user-service under `/api/v1/user/admin/users/stats/`.
 * The frontend admin dashboard merges both responses.
 */
@RestController
@RequestMapping("/api/v1/learning/admin/dashboard")
@Tag(name = "Admin Dashboard (Learning)", description = "Learning-domain stats")
@PreAuthorize("hasRole('ADMIN')")
class AdminDashboardController(
    private val vocabularyService: VocabularyService,
    private val categoryService: CategoryService,
    private val topicService: TopicService,
    private val flashcardCrudService: FlashcardCrudService,
) {
    private val logger = LoggerFactory.getLogger(AdminDashboardController::class.java)

    @GetMapping("/stats", produces = [MediaType.APPLICATION_JSON_VALUE])
    @Operation(
        summary = "Get learning-domain dashboard statistics",
        security = [SecurityRequirement(name = "bearerAuth")],
    )
    fun getDashboardStats(): ResponseEntity<Any> {
        logger.info("Fetching learning-domain dashboard statistics")

        val now = LocalDateTime.now()
        val startOfDay =
            now
                .withHour(0)
                .withMinute(0)
                .withSecond(0)
                .withNano(0)

        val stats =
            mapOf(
                "vocabularyCount" to vocabularyService.getVocabularyCount(),
                "categoryCount" to categoryService.getCategoryCount(),
                "topicCount" to topicService.getTopicCount(),
                "flashcardsCreatedToday" to flashcardCrudService.getFlashcardsCreatedCount(startOfDay),
                "flashcardsStudiedToday" to flashcardCrudService.getFlashcardsStudiedCount(startOfDay),
                "searchesToday" to 0,
            )

        return ResponseEntity.ok(stats)
    }
}
