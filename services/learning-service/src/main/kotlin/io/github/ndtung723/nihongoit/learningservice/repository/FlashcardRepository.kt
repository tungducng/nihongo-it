package io.github.ndtung723.nihongoit.learningservice.repository

import io.github.ndtung723.nihongoit.learningservice.entity.FlashcardEntity
import org.springframework.data.domain.Page
import org.springframework.data.domain.Pageable
import org.springframework.data.jpa.repository.EntityGraph
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.jpa.repository.Query
import org.springframework.data.repository.query.Param
import org.springframework.stereotype.Repository
import java.time.LocalDateTime
import java.util.UUID

@Repository
@Suppress("FunctionNaming", "ktlint:standard:function-naming")
interface FlashcardRepository : JpaRepository<FlashcardEntity, UUID> {
    @Query(
        "SELECT f FROM FlashcardEntity f LEFT JOIN FETCH f.vocabulary " +
            "WHERE f.userId = :userId AND f.due <= :now ORDER BY f.due",
    )
    fun findDueCards(
        @Param("userId") userId: UUID,
        @Param("now") now: LocalDateTime,
    ): List<FlashcardEntity>

    @EntityGraph(attributePaths = ["vocabulary"])
    fun findByUserId(userId: UUID): List<FlashcardEntity>

    @EntityGraph(attributePaths = ["vocabulary"])
    fun findByUserId(
        userId: UUID,
        pageable: Pageable,
    ): Page<FlashcardEntity>

    fun findByVocabulary_VocabId(vocabId: UUID): List<FlashcardEntity>

    @EntityGraph(attributePaths = ["vocabulary"])
    fun findByUserIdAndVocabulary_VocabId(
        userId: UUID,
        vocabId: UUID,
    ): List<FlashcardEntity>

    fun countByCreatedAtAfter(createdAt: LocalDateTime): Long
}
