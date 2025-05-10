package com.example.nihongoit.scheduled

import com.example.nihongoit.repository.UserRepository
import com.example.nihongoit.repository.ReviewLogRepository
import org.slf4j.LoggerFactory
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.scheduling.annotation.Scheduled
import org.springframework.stereotype.Component
import org.springframework.transaction.annotation.Transactional
import java.time.LocalDateTime
import java.time.LocalDate

/**
 * Scheduled job to track and update user streaks
 * This job runs every day at midnight to check which users have
 * maintained their streak and which ones need to be reset
 */
@Component
class StreakTrackerJob @Autowired constructor(
    private val userRepository: UserRepository,
    private val reviewLogRepository: ReviewLogRepository
) {
    private val logger = LoggerFactory.getLogger(StreakTrackerJob::class.java)
    
    /**
     * Reset streak counts for users who haven't completed reviews today
     * Runs daily at 11:59 PM
     */
    @Scheduled(cron = "0 59 23 * * *") // Run at 11:59 PM every day
    @Transactional
    fun resetStreaks() {
        logger.info("Running streak reset job")
        
        // Get yesterday's date
        val yesterday = LocalDate.now().minusDays(1)
        val startOfYesterday = yesterday.atStartOfDay()
        val endOfYesterday = yesterday.plusDays(1).atStartOfDay().minusSeconds(1)
        
        // Get all active users
        val allUsers = userRepository.findByIsActiveTrue()
        logger.info("Found ${allUsers.size} active users")
        
        // Process each user
        var resetCount = 0
        
        allUsers.forEach { user ->
            // Check if user studied yesterday
            val recentReviews = reviewLogRepository.findByUserIdAndReviewTimestampBetween(
                user.userId!!, startOfYesterday, endOfYesterday
            )
            
            if (recentReviews.isEmpty() && user.streakCount > 0) {
                // No reviews yesterday, reset streak
                val updatedUser = user.copy(
                    streakCount = 0,
                    updatedAt = LocalDateTime.now()
                )
                userRepository.save(updatedUser)
                resetCount++
                
                logger.debug("Reset streak for user ${user.email}")
            }
        }
        
        logger.info("Streak reset job completed. Reset streaks for $resetCount users")
    }
} 