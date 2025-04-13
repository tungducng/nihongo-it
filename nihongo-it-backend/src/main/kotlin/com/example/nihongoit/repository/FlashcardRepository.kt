package com.example.nihongoit.repository

import com.example.nihongoit.entity.FlashcardEntity
import com.example.nihongoit.entity.UserEntity
import com.example.nihongoit.entity.VocabularyEntity
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.jpa.repository.Query
import org.springframework.data.repository.query.Param
import org.springframework.stereotype.Repository
import java.time.LocalDateTime
import java.util.UUID

@Repository
interface FlashcardRepository : JpaRepository<FlashcardEntity, UUID> {
    @Query("SELECT f FROM FlashcardEntity f WHERE f.user.userId = :userId AND f.due <= :now ORDER BY f.due")
    fun findDueCards(@Param("userId") userId: UUID, @Param("now") now: LocalDateTime): List<FlashcardEntity>
    
    fun findByUser(user: UserEntity): List<FlashcardEntity>
    
    fun findByUser_UserId(userId: UUID): List<FlashcardEntity>
    
    fun countByUser(user: UserEntity): Long
    
    fun countByUser_UserId(userId: UUID): Long
    
    fun findByVocabulary(vocabulary: VocabularyEntity): List<FlashcardEntity>
    
    fun findByVocabulary_VocabId(vocabId: UUID): List<FlashcardEntity>
    
    fun findByUser_UserIdAndVocabulary_VocabId(userId: UUID, vocabId: UUID): List<FlashcardEntity>
    
    @Query("SELECT f FROM FlashcardEntity f WHERE f.user.userId = :userId AND f.due BETWEEN :now AND :futureDate ORDER BY f.due")
    fun findUpcomingCards(
        @Param("userId") userId: UUID,
        @Param("now") now: LocalDateTime,
        @Param("futureDate") futureDate: LocalDateTime
    ): List<FlashcardEntity>
    
    @Query("SELECT f FROM FlashcardEntity f WHERE f.user.userId = :userId AND f.lapses >= 5")
    fun findLeechCards(@Param("userId") userId: UUID): List<FlashcardEntity>
}

