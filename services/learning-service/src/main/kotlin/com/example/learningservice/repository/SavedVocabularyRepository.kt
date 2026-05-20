package com.example.learningservice.repository

import com.example.learningservice.entity.SavedVocabularyEntity
import com.example.learningservice.entity.SavedVocabularyId
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.jpa.repository.Modifying
import org.springframework.data.jpa.repository.Query
import org.springframework.data.repository.query.Param
import org.springframework.stereotype.Repository
import java.util.UUID

@Repository
interface SavedVocabularyRepository : JpaRepository<SavedVocabularyEntity, SavedVocabularyId> {
    fun existsByVocabIdAndUserId(
        vocabId: UUID,
        userId: UUID,
    ): Boolean

    @Modifying
    @Query("DELETE FROM SavedVocabularyEntity s WHERE s.vocabId = :vocabId AND s.userId = :userId")
    fun deleteByVocabAndUser(
        @Param("vocabId") vocabId: UUID,
        @Param("userId") userId: UUID,
    ): Int

    fun findByUserId(userId: UUID): List<SavedVocabularyEntity>
}
