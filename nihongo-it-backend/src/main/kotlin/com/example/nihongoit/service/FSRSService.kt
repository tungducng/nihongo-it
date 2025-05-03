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

/**
 * Free Spaced Repetition Scheduler (FSRS) 4.0 implementation
 * Based on the open-spaced-repetition/go-fsrs algorithm
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

        // Calculate elapsed time since due date
        val now = LocalDateTime.now()
        val elapsedDays = if (flashcard.due.isBefore(now)) {
            ChronoUnit.DAYS.between(flashcard.due, now).toDouble().coerceAtLeast(0.0)
        } else {
            0.0
        }

        // Get scheduling info for this rating
        val info = calculateSchedulingInfo(flashcard, rating, elapsedDays, now)

        // Update flashcard with new values
        flashcard.difficulty = info.difficulty
        flashcard.stability = info.stability
        flashcard.state = info.state
        flashcard.elapsedDays = info.elapsedDays
        flashcard.scheduledDays = info.scheduledDays
        flashcard.due = info.due
        flashcard.reps += 1
        if (rating == Rating.AGAIN) flashcard.lapses += 1
        flashcard.updatedAt = now

        return flashcardRepository.save(flashcard)
    }

    /**
     * Initialize a new flashcard with default FSRS values
     */
    @Transactional
    fun initializeFlashcard(flashcard: FlashcardEntity): FlashcardEntity {
        logger.info("Initializing new flashcard with FSRS")

        // Set default values
        flashcard.difficulty = 0.3  // Default difficulty
        flashcard.stability = 0.5   // Default stability
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

        // Calculate new due date
        val due = now.plusDays(interval.toLong())

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
        return when {
            state == State.NEW || state == State.LEARNING || state == State.RELEARNING -> {
                when (rating) {
                    Rating.AGAIN -> 0.0  // Same day
                    Rating.HARD -> 1.0   // Next day
                    Rating.GOOD -> 3.0   // Three days
                    Rating.EASY -> 7.0   // One week
                }
            }
            else -> {
                // For review state, use FSRS interval calculation
                val interval = nextInterval(stability)
                when (rating) {
                    Rating.AGAIN -> 0.0  // Same day
                    Rating.HARD -> max(1.0, interval * 0.5)  // Half the interval
                    Rating.GOOD -> max(1.0, interval)        // Normal interval
                    Rating.EASY -> max(1.0, interval * 1.5)  // 1.5x the interval
                }
            }
        }.coerceAtMost(maximumInterval)
    }

    /**
     * Calculates the next review interval based on stability
     */
    private fun nextInterval(stability: Double): Double {
        val interval = stability * 9.0 * (1.0 / requestRetention - 1.0)
        return min(interval, maximumInterval)
    }

    /**
     * Updates the difficulty score based on the rating
     */
    private fun updateDifficulty(oldDifficulty: Double, rating: Rating): Double {
        val newDifficulty = oldDifficulty - w[15] * (rating.value - 3) + w[16]
        return max(min(newDifficulty, 1.0), 0.1)  // Clamp between 0.1 and 1.0
    }

    /**
     * Updates the stability score based on rating, difficulty, and retrievability
     */
    private fun updateStability(oldStability: Double, difficulty: Double, rating: Rating, retrievability: Double): Double {
        return when (rating) {
            Rating.AGAIN -> oldStability * w[0]  // Again - significant decrease
            Rating.HARD -> oldStability * (w[1] + w[2] * (1 - retrievability) * difficulty)  // Hard
            Rating.GOOD -> oldStability * (w[3] + w[4] * (1 - retrievability) * difficulty)  // Good
            Rating.EASY -> oldStability * (w[5] + w[6] * (1 - retrievability) * difficulty)  // Easy - significant increase
        }
    }

    /**
     * Calculates the retrievability score based on spacing effect
     * R = e^(-t/S) where t is time elapsed and S is stability
     */
    private fun calculateRetrievability(stability: Double, elapsedDays: Double): Double {
        return exp(-elapsedDays / stability.coerceAtLeast(1.0))
    }

    /**
     * Determines the new state of the card based on current state and rating
     */
    private fun determineState(oldState: State, rating: Rating): State {
        return when {
            rating == Rating.AGAIN -> State.RELEARNING  // Any "Again" rating goes to relearning
            oldState == State.NEW || oldState == State.RELEARNING ->
                if (rating.value >= 3) State.REVIEW else State.LEARNING  // New/relearning -> review/learning
            else -> State.REVIEW  // Stay in review
        }
    }

    /**
     * Simulate and get all possible scheduling outcomes for a card
     * Similar to the Parameters.Repeat method in Go-FSRS
     */
    fun simulateReview(card: FlashcardEntity): Map<Rating, SchedulingInfo> {
        val now = LocalDateTime.now()
        val elapsedDays = ChronoUnit.DAYS.between(card.due, now).toDouble().coerceAtLeast(0.0)

        return Rating.values().associate { rating ->
            rating to calculateSchedulingInfo(card, rating, elapsedDays, now)
        }
    }
}