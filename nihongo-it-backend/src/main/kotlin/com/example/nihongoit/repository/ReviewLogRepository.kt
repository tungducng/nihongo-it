package com.example.nihongoit.repository

import com.example.nihongoit.entity.ReviewLogEntity
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.jpa.repository.Query
import org.springframework.data.repository.query.Param
import org.springframework.stereotype.Repository
import java.time.LocalDateTime
import java.util.UUID

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
} 