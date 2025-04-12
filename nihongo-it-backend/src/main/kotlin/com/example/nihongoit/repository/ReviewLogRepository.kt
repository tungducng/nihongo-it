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
    fun findByFlashcardFlashCardId(flashcardId: UUID): List<ReviewLogEntity>
    
    fun findByUserIdOrderByTimestampDesc(userId: UUID): List<ReviewLogEntity>
    
    @Query("SELECT r FROM ReviewLogEntity r WHERE r.userId = :userId AND r.timestamp >= :startDate")
    fun findByUserIdAndTimestampAfter(
        @Param("userId") userId: UUID, 
        @Param("startDate") startDate: LocalDateTime
    ): List<ReviewLogEntity>
}