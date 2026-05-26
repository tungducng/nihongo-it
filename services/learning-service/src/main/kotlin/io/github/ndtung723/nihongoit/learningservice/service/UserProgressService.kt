package io.github.ndtung723.nihongoit.learningservice.service

import io.github.ndtung723.nihongoit.learningservice.entity.UserProgressEntity
import io.github.ndtung723.nihongoit.learningservice.repository.ReviewLogRepository
import io.github.ndtung723.nihongoit.learningservice.repository.UserProgressRepository
import org.slf4j.LoggerFactory
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import java.time.LocalDateTime
import java.util.UUID

/**
 * Owns the learning-domain `user_progress` row for each user — streak counter,
 * lifetime points, daily goal, last study date. Created lazily on the first
 * flashcard review for that user. No FK or any other coupling to user-service.
 */
@Service
class UserProgressService(
    private val userProgressRepository: UserProgressRepository,
    private val reviewLogRepository: ReviewLogRepository,
) {
    private val logger = LoggerFactory.getLogger(UserProgressService::class.java)

    /**
     * Return the current progress row for [userId], creating a default one if
     * none exists yet. Read paths can use this directly.
     */
    @Transactional
    fun getOrCreate(userId: UUID): UserProgressEntity =
        userProgressRepository
            .findById(userId)
            .orElseGet { userProgressRepository.save(UserProgressEntity(userId = userId)) }

    /**
     * Called from the flashcard review hot path. Decides the new streak value
     * based on the previous review's date, then persists.
     */
    @Transactional
    fun updateStreak(userId: UUID) {
        val progress = getOrCreate(userId)
        val now = LocalDateTime.now()
        val today = now.toLocalDate()
        val yesterday = today.minusDays(1)

        // The "previous" review here is intentionally the latest log row —
        // the review that triggered this call has already been written by
        // FlashcardCrudService.processReview.
        val previousReview = reviewLogRepository.findTopByUserIdOrderByReviewTimestampDesc(userId)
        val previousDate = previousReview?.reviewTimestamp?.toLocalDate()

        val newStreak =
            when {
                previousDate == null -> 1
                previousDate == today && progress.streakCount == 0 -> 1
                previousDate == today -> progress.streakCount
                previousDate == yesterday -> progress.streakCount + 1
                else -> 1
            }

        if (newStreak != progress.streakCount || progress.lastStudyDate?.toLocalDate() != today) {
            progress.streakCount = newStreak
            progress.lastStudyDate = now
            userProgressRepository.save(progress)
            logger.info("user_progress updated: userId=$userId streak=$newStreak")
        }
    }
}
