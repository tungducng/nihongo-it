package com.example.nihongoit.repository

import com.example.nihongoit.entity.JLPTLevel
import com.example.nihongoit.entity.VocabularyEntity
import org.springframework.data.domain.Page
import org.springframework.data.domain.Pageable
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.jpa.repository.Query
import org.springframework.data.repository.query.Param
import org.springframework.stereotype.Repository
import java.util.UUID

@Repository
interface VocabularyRepository : JpaRepository<VocabularyEntity, UUID> {

    // Basic CRUD operations are inherited from JpaRepository

    // Find by JLPT level
    @Query("SELECT v FROM VocabularyEntity v WHERE v.jlptLevel = :level")
    fun findByJlptLevel(@Param("level") level: JLPTLevel, pageable: Pageable): Page<VocabularyEntity>

    // Find by IT category (programming, network, database, AI, etc.)
    fun findByCategory(category: String, pageable: Pageable): Page<VocabularyEntity>

    // Search by keyword in kanji, hiragana, katakana, or meaning
    @Query(
        "SELECT v FROM VocabularyEntity v WHERE " +
            "v.kanji LIKE %:keyword% OR " +
            "v.hiragana LIKE %:keyword% OR " +
            "v.katakana LIKE %:keyword% OR " +
            "v.meaning LIKE %:keyword%",
    )
    fun searchVocabulary(@Param("keyword") keyword: String, pageable: Pageable): Page<VocabularyEntity>

    // Find saved vocabulary for a specific user
    @Query("SELECT v FROM VocabularyEntity v JOIN v.savedByUsers u WHERE u.userId = :userId")
    fun findSavedByUser(@Param("userId") userId: UUID, pageable: Pageable): Page<VocabularyEntity>
} 
