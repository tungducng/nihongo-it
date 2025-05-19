package com.example.userservice.repository

import com.example.userservice.entity.ReviewLogEntity
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.jpa.repository.Query
import org.springframework.data.repository.query.Param
import org.springframework.stereotype.Repository
import java.time.LocalDateTime
import java.util.*

@Repository
interface ReviewLogRepository : JpaRepository<ReviewLogEntity, UUID> {

    fun findByUserId(userId: UUID): List<ReviewLogEntity>
    
    fun countByUserId(userId: UUID): Int
    
    fun findByUserIdAndReviewTimestampAfter(userId: UUID, after: LocalDateTime): List<ReviewLogEntity>
    
    @Query("SELECT COUNT(DISTINCT r.flashcard.id) FROM ReviewLogEntity r WHERE r.reviewTimestamp > :after")
    fun countDistinctFlashcardIdByReviewTimestampAfter(@Param("after") after: LocalDateTime): Long
    
    @Query("""
        SELECT CAST(CAST(review_timestamp AS DATE) AS VARCHAR) as date, 
               COUNT(review_log_id) as count,
               AVG(CAST(rating AS FLOAT)) as avg_rating
        FROM review_logs
        WHERE user_id = :userId AND review_timestamp BETWEEN :startDate AND :endDate
        GROUP BY CAST(review_timestamp AS DATE)
        ORDER BY CAST(review_timestamp AS DATE)
    """, nativeQuery = true)
    fun getDailyReviewStats(
        @Param("userId") userId: UUID,
        @Param("startDate") startDate: LocalDateTime,
        @Param("endDate") endDate: LocalDateTime
    ): List<Map<String, Any>>
    
    @Query("""
        SELECT COUNT(rl) as count, AVG(rl.rating) as avgRating
        FROM ReviewLogEntity rl
        WHERE rl.userId = :userId AND rl.reviewTimestamp >= :since
    """)
    fun getReviewStats(
        @Param("userId") userId: UUID,
        @Param("since") since: LocalDateTime
    ): Map<String, Any>

    // Find reviews for a specific flashcard
    fun findByFlashcard_FlashcardId(flashcardId: UUID): List<ReviewLogEntity>
    
    // Find reviews by user ID ordered by timestamp
    fun findByUserIdOrderByReviewTimestampDesc(userId: UUID): List<ReviewLogEntity>
    
    // Find reviews by user ID and date range
    fun findByUserIdAndReviewTimestampAfterOrderByReviewTimestampDesc(
        userId: UUID, 
        startDate: LocalDateTime
    ): List<ReviewLogEntity>
    
    // Find reviews by user ID and date range (between two timestamps)
    fun findByUserIdAndReviewTimestampBetween(
        userId: UUID,
        startTimestamp: LocalDateTime,
        endTimestamp: LocalDateTime
    ): List<ReviewLogEntity>
    
    // Find most recent review by user ID
    fun findFirstByUserIdOrderByReviewTimestampDesc(userId: UUID): ReviewLogEntity?
    
    // Count reviews by user ID and date range
    fun countByUserIdAndReviewTimestampAfter(userId: UUID, startDate: LocalDateTime): Int
    
    // Find reviews after a specific date
    fun findByReviewTimestampAfter(startDate: LocalDateTime): List<ReviewLogEntity>
    
    // Count reviews by day for a user
    @Query("SELECT CAST(r.reviewTimestamp as date) as reviewDate, COUNT(r) " +
           "FROM ReviewLogEntity r " +
           "WHERE r.userId = :userId AND r.reviewTimestamp > :startDate " +
           "GROUP BY CAST(r.reviewTimestamp as date) " +
           "ORDER BY reviewDate DESC")
    fun countReviewsByDayForUser(
        @Param("userId") userId: UUID,
        @Param("startDate") startDate: LocalDateTime
    ): List<Array<Any>>
    
    // Count correct reviews by day for a user (rating >= 3)
    @Query("SELECT CAST(r.reviewTimestamp as date) as reviewDate, COUNT(r) " +
           "FROM ReviewLogEntity r " +
           "WHERE r.userId = :userId AND r.reviewTimestamp > :startDate AND r.rating >= 3 " +
           "GROUP BY CAST(r.reviewTimestamp as date) " +
           "ORDER BY reviewDate DESC")
    fun countCorrectReviewsByDayForUser(
        @Param("userId") userId: UUID,
        @Param("startDate") startDate: LocalDateTime
    ): List<Array<Any>>

    @Query(value = "SELECT COUNT(DISTINCT r.flashcard.id) FROM ReviewLogEntity r WHERE r.userId = :userId AND r.reviewTimestamp > :timestamp")
    fun countDistinctFlashcardIdByUserIdAndReviewTimestampAfter(userId: UUID, timestamp: LocalDateTime): Int

    fun findTopByUserIdOrderByReviewTimestampDesc(userId: UUID): ReviewLogEntity?
} 