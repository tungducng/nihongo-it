package com.example.nihongoit.repository

import com.example.nihongoit.entity.FlashcardEntity
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.jpa.repository.Query
import org.springframework.data.repository.query.Param
import org.springframework.stereotype.Repository
import java.time.LocalDateTime
import java.util.UUID

@Repository
interface FlashcardRepository : JpaRepository<FlashcardEntity, UUID> {
    @Query("SELECT f FROM FlashcardEntity f WHERE f.userId = :userId AND f.due <= :now ORDER BY f.due")
    fun findDueCards(@Param("userId") userId: UUID, @Param("now") now: LocalDateTime): List<FlashcardEntity>
    
    fun findByUserId(userId: UUID): List<FlashcardEntity>
    
    fun countByUserId(userId: UUID): Long
}

