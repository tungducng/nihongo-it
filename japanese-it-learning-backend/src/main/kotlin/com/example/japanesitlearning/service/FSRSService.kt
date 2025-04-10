package com.example.japanesitlearning.service

import com.example.japanesitlearning.entity.FlashcardEntity
import org.slf4j.LoggerFactory
import org.springframework.stereotype.Service
import java.time.LocalDateTime
import java.time.temporal.ChronoUnit
import kotlin.math.max
import kotlin.math.min

/**
 * Free Spaced Repetition Scheduler (FSRS) implementation for Japanese IT Learning
 * Based on the algorithm created by L. Piotr Wozniak for SuperMemo
 */
@Service
class FSRSService {
    private val logger = LoggerFactory.getLogger(FSRSService::class.java)
    
    // Các tham số của FSRS
    private val w = doubleArrayOf(0.4, 0.6, 2.4, 5.8, 4.93, 0.94, 0.86, 0.01, 1.49, 0.14, 0.94, 2.18, 0.05, 0.34, 1.26, 0.29, 2.61)
    private val requestRetention = 0.9  // Target retention rate (90%)
    private val maxInterval = 36500.0   // Maximum interval (100 years)
    
    /**
     * Calculates the next review interval based on stability
     * @param stability The card's stability score
     * @return The next interval in days
     */
    private fun nextInterval(stability: Double): Double {
        val interval = stability * 9.0 * (1.0 / requestRetention - 1.0)
        return min(interval, maxInterval)
    }
    
    /**
     * Updates card parameters based on the user's rating
     * @param card The flashcard entity to update
     * @param rating The user's rating (1-4)
     * @return FSRSResult with updated parameters
     */
    fun updateCardParameters(card: FlashcardEntity, rating: Int): FSRSResult {
        logger.info("Updating card parameters - Card ID: ${card.flashCardId}, Rating: $rating, Current State: ${card.state}")
        
        val now = LocalDateTime.now()
        val elapsedDays = ChronoUnit.DAYS.between(card.due, now).toDouble().coerceAtLeast(0.0)
        
        // Tính toán các thông số dựa trên thuật toán FSRS
        val difficulty = updateDifficulty(card.difficulty, rating)
        val stability = updateStability(card.stability, card.difficulty, rating, elapsedDays)
        val newState = determineState(card.state, rating)
        val interval = nextInterval(stability)
        
        logger.debug("Updated parameters - Difficulty: $difficulty, Stability: $stability, New State: $newState, Next Interval: $interval")
        
        return FSRSResult(
            difficulty = difficulty,
            stability = stability,
            state = newState,
            interval = interval
        )
    }
    
    /**
     * Updates the difficulty score based on the rating
     * @param oldDifficulty The previous difficulty score
     * @param rating The user's rating (1-4)
     * @return Updated difficulty value
     */
    private fun updateDifficulty(oldDifficulty: Double, rating: Int): Double {
        val newDifficulty = oldDifficulty - w[15] * (rating - 3) + w[16]
        return max(min(newDifficulty, 1.0), 0.1)  // Clamp between 0.1 and 1.0
    }
    
    /**
     * Updates the stability score based on rating, difficulty, and elapsed time
     * @param oldStability The previous stability score
     * @param difficulty The difficulty score
     * @param rating The user's rating (1-4)
     * @param elapsedDays Days elapsed since due date
     * @return Updated stability value
     */
    private fun updateStability(oldStability: Double, difficulty: Double, rating: Int, elapsedDays: Double): Double {
        val retrievability = calculateRetrievability(oldStability, elapsedDays)
        
        return when (rating) {
            1 -> oldStability * w[0]  // Again - significant decrease
            2 -> oldStability * (w[1] + w[2] * (1 - retrievability) * difficulty)  // Hard
            3 -> oldStability * (w[3] + w[4] * (1 - retrievability) * difficulty)  // Good
            4 -> oldStability * (w[5] + w[6] * (1 - retrievability) * difficulty)  // Easy - significant increase
            else -> oldStability      // Fallback
        }
    }
    
    /**
     * Calculates the retrievability score based on spacing effect
     * @param stability The stability score
     * @param elapsedDays Days elapsed since due date
     * @return Retrievability score between 0 and 1
     */
    private fun calculateRetrievability(stability: Double, elapsedDays: Double): Double {
        return Math.exp(-elapsedDays / stability.coerceAtLeast(1.0))
    }
    
    /**
     * Determines the new state of the card based on current state and rating
     * States: 0=New, 1=Learning, 2=Review, 3=Relearning
     * @param oldState The current state
     * @param rating The user's rating (1-4)
     * @return The new state
     */
    private fun determineState(oldState: Int, rating: Int): Int {
        return when {
            rating == 1 -> 3  // Any "Again" rating goes to relearning
            oldState == 0 || oldState == 3 -> if (rating >= 3) 2 else 1  // New/relearning -> review/learning
            else -> 2  // Stay in review
        }
    }
}

/**
 * Result class for FSRS calculations
 */
data class FSRSResult(
    val difficulty: Double,
    val stability: Double,
    val state: Int,
    val interval: Double
)