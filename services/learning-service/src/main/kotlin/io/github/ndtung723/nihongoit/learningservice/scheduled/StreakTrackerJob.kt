package io.github.ndtung723.nihongoit.learningservice.scheduled

import io.github.ndtung723.nihongoit.learningservice.repository.ReviewLogRepository
import io.github.ndtung723.nihongoit.learningservice.repository.UserProgressRepository
import org.slf4j.LoggerFactory
import org.springframework.scheduling.annotation.Scheduled
import org.springframework.stereotype.Component
import org.springframework.transaction.annotation.Transactional
import java.time.LocalDate
import java.time.LocalDateTime
import java.util.UUID

/**
 * Nightly job that maintains `user_progress.streak_count`:
 *   • reviewed today or yesterday — keep streak unchanged
 *   • reviewed 2 days ago and streak > 1 — decrement (grace period)
 *   • otherwise — reset to 0
 *
 * Operates entirely on `user_progress` rows owned by learning-service — no
 * cross-service calls.
 */
@Component
class StreakTrackerJob(
    private val userProgressRepository: UserProgressRepository,
    private val reviewLogRepository: ReviewLogRepository,
) {
    private val logger = LoggerFactory.getLogger(StreakTrackerJob::class.java)

    companion object {
        private const val COL_USER_ID = 0
        private const val COL_TODAY = 1
        private const val COL_YESTERDAY = 2
        private const val COL_TWO_DAYS_AGO = 3
    }

    @Scheduled(cron = "0 59 23 * * *")
    @Transactional
    fun resetStreaks() {
        logger.info("Running streak reset job at ${LocalDateTime.now()}")

        val today = LocalDate.now()
        val startOfToday = today.atStartOfDay()
        val startOfYesterday = today.minusDays(1).atStartOfDay()
        val startOfTwoDaysAgo = today.minusDays(2).atStartOfDay()

        val progresses = userProgressRepository.findAll().filter { it.streakCount > 0 }
        if (progresses.isEmpty()) {
            logger.info("No user_progress rows with positive streak — skipping")
            return
        }

        val userIds = progresses.map { it.userId }
        val activityRows =
            reviewLogRepository.getUserActivitySummary(
                userIds,
                startOfToday,
                startOfYesterday,
                startOfTwoDaysAgo,
            )
        val activityMap: Map<UUID, Triple<Boolean, Boolean, Boolean>> =
            activityRows.associate { row ->
                val uid = UUID.fromString(row[COL_USER_ID].toString())
                val reviewedToday = (row[COL_TODAY] as Number).toInt() > 0
                val reviewedYesterday = (row[COL_YESTERDAY] as Number).toInt() > 0
                val reviewedTwoDaysAgo = (row[COL_TWO_DAYS_AGO] as Number).toInt() > 0
                uid to Triple(reviewedToday, reviewedYesterday, reviewedTwoDaysAgo)
            }

        var resetCount = 0
        var reducedCount = 0

        val updated =
            progresses.mapNotNull { progress ->
                val (reviewedToday, reviewedYesterday, reviewedTwoDaysAgo) =
                    activityMap[progress.userId] ?: Triple(false, false, false)

                when {
                    reviewedToday || reviewedYesterday -> null
                    reviewedTwoDaysAgo && progress.streakCount > 1 -> {
                        reducedCount++
                        progress.also { it.streakCount = it.streakCount - 1 }
                    }
                    else -> {
                        resetCount++
                        progress.also { it.streakCount = 0 }
                    }
                }
            }

        if (updated.isNotEmpty()) {
            userProgressRepository.saveAll(updated)
        }

        logger.info(
            "Streak job done: processed ${progresses.size} progress rows, " +
                "reset $resetCount, reduced $reducedCount",
        )
    }
}
