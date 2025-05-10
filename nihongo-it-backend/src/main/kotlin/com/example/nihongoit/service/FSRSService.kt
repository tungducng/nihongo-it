package com.example.nihongoit.service

import com.example.nihongoit.entity.FlashcardEntity
import com.example.nihongoit.repository.FlashcardRepository
import org.slf4j.LoggerFactory
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import java.time.LocalDateTime
import java.time.temporal.ChronoUnit
import kotlin.math.max
import kotlin.math.min
import kotlin.math.exp
import kotlin.math.pow
import kotlin.math.exp

/**
 * Free Spaced Repetition Scheduler (FSRS) 4.0 implementation
 * Based on the FSRS v4 algorithm
 */
@Service
class FSRSService @Autowired constructor(
    private val flashcardRepository: FlashcardRepository
) {
    private val logger = LoggerFactory.getLogger(FSRSService::class.java)

    // FSRS algorithm parameters
    private val w = doubleArrayOf(0.4, 0.6, 2.4, 5.8, 4.93, 0.94, 0.86, 0.01, 1.49, 0.14, 0.94, 2.18, 0.05, 0.34, 1.26, 0.29, 2.61)
    private val requestRetention = 0.9  // Target retention rate (90%)
    private val maximumInterval = 36500.0  // Maximum interval (100 years)
    
    /**
     * Rating enum similar to the Go implementation
     */
    enum class Rating(val value: Int) {
        AGAIN(1),
        HARD(2),
        GOOD(3),
        EASY(4);

        companion object {
            fun fromInt(value: Int): Rating = when(value) {
                1 -> AGAIN
                2 -> HARD
                3 -> GOOD
                4 -> EASY
                else -> throw IllegalArgumentException("Invalid rating: $value")
            }
        }
    }

    /**
     * State enum similar to the Go implementation
     */
    enum class State(val value: Int) {
        NEW(0),
        LEARNING(1),
        REVIEW(2),
        RELEARNING(3);

        companion object {
            fun fromInt(value: Int): State = State.entries.first { it.value == value }
        }
    }

    /**
     * SchedulingInfo class similar to the Go implementation
     */
    data class SchedulingInfo(
        val due: LocalDateTime,
        val stability: Double,
        val difficulty: Double,
        val elapsedDays: Double,
        val scheduledDays: Double,
        val state: Int,
        val reps: Int,
        val lapses: Int
    )

    /**
     * Process a flashcard review
     */
    @Transactional
    fun processReview(flashcard: FlashcardEntity, ratingValue: Int): FlashcardEntity {
        val rating = Rating.fromInt(ratingValue)
        logger.info("Processing review with FSRS - Flashcard ID: ${flashcard.flashcardId}, Rating: $rating")
        logger.info("Previous state: State=${State.fromInt(flashcard.state)}, Difficulty=${flashcard.difficulty}, Stability=${flashcard.stability}, Reps=${flashcard.reps}")

        // Calculate elapsed time since due date
        val now = LocalDateTime.now()
        val elapsedDays = if (flashcard.due.isBefore(now)) {
            ChronoUnit.MILLIS.between(flashcard.due, now).toDouble() / (1000 * 60 * 60 * 24)
        } else {
            0.0
        }
        
        // Calculate retrievability before making changes
        val retrievability = calculateRetrievability(flashcard.stability, elapsedDays)
        logger.info("Review timing: Elapsed days=$elapsedDays, Retrievability=$retrievability")

        // Get scheduling info for this rating
        val info = calculateSchedulingInfo(flashcard, rating, elapsedDays, now)
        logger.info("New scheduling: State=${State.fromInt(info.state)}, Difficulty=${info.difficulty}, Stability=${info.stability}")
        logger.info("Next due: ${info.due}, Interval: ${info.scheduledDays} days")

        // Update flashcard with new values
        flashcard.difficulty = info.difficulty
        flashcard.stability = info.stability
        flashcard.state = info.state
        flashcard.elapsedDays = info.elapsedDays
        flashcard.scheduledDays = info.scheduledDays
        
        // Xử lý đặc biệt cho Again rating
        if (rating == Rating.AGAIN) {
            // Thêm thông tin về khoảng thời gian chính xác (theo phút)
            val minutesToAdd = 30.0 // Cố định 15 phút thay vì ngẫu nhiên
            flashcard.due = now.plusMinutes(minutesToAdd.toLong())
            logger.info("Again rating: Due again in $minutesToAdd minutes")
            flashcard.lapses += 1
        } else {
            // Sử dụng thời gian chính xác thay vì làm tròn đến ngày
            val intervalInDays = info.scheduledDays
            val days = intervalInDays.toLong()
            val hours = ((intervalInDays - days) * 24).toLong()
            flashcard.due = now.plusDays(days).plusHours(hours)
        }
        
        flashcard.reps += 1
        flashcard.updatedAt = now

        return flashcardRepository.save(flashcard)
    }

    /**
     * Initialize a new flashcard with default FSRS values
     * S_0(G) = w_{G-1} (Initial stability for rating G)
     * D_0(G) = w_4 - (G-3) * w_5 (Initial difficulty for rating G)
     * 
     * For new cards:
     * - Initial stability (G=3, Good): S_0(3) = w_2 = 2.4
     * - Initial difficulty (G=3, Good): D_0(3) = w_4 = 4.93
     */
    @Transactional
    fun initializeFlashcard(flashcard: FlashcardEntity): FlashcardEntity {
        logger.info("Initializing new flashcard with FSRS")
        
        // S_0(G) = w_{G-1} for a new card rated as "Good" (G=3)
        // Initial stability is w_2
        flashcard.stability = w[2]
        
        // D_0(G) = w_4 - (G-3) * w_5 for a new card rated as "Good" (G=3)
        // Initial difficulty is w_4
        flashcard.difficulty = w[4]
        
        flashcard.state = State.NEW.value
        flashcard.elapsedDays = 0.0
        flashcard.scheduledDays = 1.0
        flashcard.due = LocalDateTime.now()  // Due immediately
        flashcard.reps = 0
        flashcard.lapses = 0

        return flashcardRepository.save(flashcard)
    }

    /**
     * Calculate the scheduling information based on the card state and rating
     */
    private fun calculateSchedulingInfo(
        card: FlashcardEntity,
        rating: Rating,
        elapsedDays: Double,
        now: LocalDateTime
    ): SchedulingInfo {
        // Current state
        val state = State.fromInt(card.state)

        // Update difficulty
        val newDifficulty = updateDifficulty(card.difficulty, rating)

        // Calculate retrievability
        val retrievability = calculateRetrievability(card.stability, elapsedDays)

        // Update stability based on rating
        val newStability = updateStability(card.stability, newDifficulty, rating, retrievability)

        // Determine new state
        val newState = determineState(state, rating)

        // Calculate next interval
        val interval = calculateInterval(newStability, rating, newState)

        // Calculate new due date with precise time (not just days)
        val days = interval.toLong()
        val hours = ((interval - days) * 24).toLong()
        val due = now.plusDays(days).plusHours(hours)

        return SchedulingInfo(
            due = due,
            stability = newStability,
            difficulty = newDifficulty,
            elapsedDays = 0.0,  // Reset elapsed days
            scheduledDays = interval,
            state = newState.value,
            reps = card.reps + 1,
            lapses = if (rating == Rating.AGAIN) card.lapses + 1 else card.lapses
        )
    }

    /**
     * Calculate interval based on stability and state
     */
    private fun calculateInterval(stability: Double, rating: Rating, state: State): Double {
        // Sử dụng công thức FSRS cho tất cả các trạng thái
        val interval = nextInterval(stability)
        logger.info("FSRS calculation - Base interval calculated from stability $stability: $interval days")
        
        val finalInterval = when (rating) {
            Rating.AGAIN -> 0.0  // Same day (handled separately in processReview)
            Rating.HARD -> max(1.0, interval * 0.7).also { 
                logger.info("HARD rating - Reducing interval by 30%: $interval days -> $it days") 
            }
            Rating.GOOD -> max(1.0, interval).also { 
                logger.info("GOOD rating - Using normal interval: $it days") 
            }
            Rating.EASY -> max(1.0, interval * 1.3).also { 
                logger.info("EASY rating - Increasing interval by 30%: $interval days -> $it days")
            }
        }.coerceAtMost(maximumInterval)
        
        logger.info("Final interval after adjustments: $finalInterval days")
        return finalInterval
    }

    /**
     * Calculates the next review interval based on stability
     * I(r,S) = 9 * S * (1/r - 1)
     */
    private fun nextInterval(stability: Double): Double {
        val requestRetentionFactor = 1.0 / requestRetention - 1.0
        logger.info("Request retention: $requestRetention, factor: $requestRetentionFactor")
        
        val interval = 9.0 * stability * requestRetentionFactor
        logger.info("FSRS formula: 9.0 * $stability * $requestRetentionFactor = $interval")
        
        val cappedInterval = min(interval, maximumInterval)
        if (cappedInterval < interval) {
            logger.info("Interval capped to maximum: $maximumInterval days")
        }
        
        return cappedInterval
    }

    /**
     * Updates the difficulty score based on the rating
     * D'(D,G) = w_7 * D_0(3) + (1-w_7) * (D - w_6 * (G-3))
     */
    private fun updateDifficulty(oldDifficulty: Double, rating: Rating): Double {
        // Initial difficulty for "Good" (G=3): D_0(3) = w_4
        val initialDifficulty = w[4]
        
        // Calculate new difficulty: D' = D - w_6 * (G-3)
        val difficultyChange = oldDifficulty - w[6] * (rating.value - 3)
        
        // Apply mean reversion: D' = w_7 * D_0(3) + (1-w_7) * D'
        val newDifficulty = w[7] * initialDifficulty + (1 - w[7]) * difficultyChange
        
        // Clamp difficulty between 1 and 10
        return max(min(newDifficulty, 10.0), 1.0)
    }

    /**
     * Updates the stability score based on rating, difficulty, and retrievability
     */
    private fun updateStability(oldStability: Double, difficulty: Double, rating: Rating, retrievability: Double): Double {
        return when (rating) {
            Rating.AGAIN -> {
                // Stability after forgetting (post-lapse stability):
                // S_f'(D,S,R) = w_11 * D^(-w_12) * ((S+1)^w_13 - 1) * e^(w_14 * (1-R))
                val forgettingStability = w[11] * Math.pow(difficulty, -w[12]) * 
                                        (Math.pow(oldStability + 1, w[13]) - 1) * 
                                        Math.exp(w[14] * (1 - retrievability))
                forgettingStability.coerceAtLeast(0.1) // Ensure minimum stability
            }
            else -> {
                // Stability after successful review:
                // S_r'(D,S,R,G) = S * (e^w_8 * (11-D) * S^(-w_9) * (e^(w_10*(1-R))-1) * w_15(if G=2) * w_16(if G=4) + 1)
                val hardMultiplier = if (rating == Rating.HARD) w[15] else 1.0
                val easyMultiplier = if (rating == Rating.EASY) w[16] else 1.0
                
                // Implementing the exact formula from the FSRS v4 document
                val stabilityIncrease = Math.exp(w[8]) * (11.0 - difficulty) * 
                                       Math.pow(oldStability, -w[9]) * 
                                       (Math.exp(w[10] * (1.0 - retrievability)) - 1.0) * 
                                       hardMultiplier * easyMultiplier
                
                val newStability = oldStability * (stabilityIncrease + 1.0)
                newStability.coerceAtLeast(0.1) // Ensure minimum stability
            }
        }
    }

    /**
     * Calculates the retrievability score based on spacing effect
     * R(t,S) = (1 + t/(9*S))^(-1)
     */
    private fun calculateRetrievability(stability: Double, elapsedDays: Double): Double {
        val safeStability = stability.coerceAtLeast(0.1)  // Avoid division by zero
        return Math.pow(1.0 + elapsedDays / (9.0 * safeStability), -1.0)
    }

    /**
     * Determines the new state of the card based on current state and rating
     */
    private fun determineState(oldState: State, rating: Rating): State {
        return when {
            rating == Rating.AGAIN -> State.RELEARNING  // Any "Again" rating goes to relearning
            oldState == State.NEW || oldState == State.LEARNING || oldState == State.RELEARNING -> {
                if (rating.value >= 3) State.REVIEW else State.LEARNING  // Move to review if Good/Easy
            }
            else -> State.REVIEW  // Stay in review
        }
    }

    /**
     * Simulate and get all possible scheduling outcomes for a card
     */
    fun simulateReview(card: FlashcardEntity): Map<Rating, SchedulingInfo> {
        val now = LocalDateTime.now()
        val elapsedDays = ChronoUnit.DAYS.between(card.due, now).toDouble().coerceAtLeast(0.0)

        return Rating.values().associate { rating ->
            rating to calculateSchedulingInfo(card, rating, elapsedDays, now)
        }
    }
}