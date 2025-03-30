package com.example.japanesitlearning.repository

import com.example.japanesitlearning.entity.VocabularyEntity
import org.springframework.data.domain.Page
import org.springframework.data.domain.Pageable
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.jpa.repository.Query
import org.springframework.data.repository.query.Param
import org.springframework.stereotype.Repository
import java.util.UUID

@Repository
interface VocabularyRepository : JpaRepository<VocabularyEntity, UUID> {

    fun findByHiraganaContainingIgnoreCase(hiragana: String, pageable: Pageable): Page<VocabularyEntity>

    fun findByMeaningContainingIgnoreCase(meaning: String, pageable: Pageable): Page<VocabularyEntity>

    @Query("SELECT v FROM VocabularyEntity v WHERE " +
           "LOWER(v.hiragana) LIKE LOWER(CONCAT('%', :keyword, '%')) OR " +
           "LOWER(v.meaning) LIKE LOWER(CONCAT('%', :keyword, '%')) OR " +
           "(:keyword IS NOT NULL AND v.kanji IS NOT NULL AND LOWER(v.kanji) LIKE LOWER(CONCAT('%', :keyword, '%')))")
    fun searchByKeyword(@Param("keyword") keyword: String, pageable: Pageable): Page<VocabularyEntity>

    fun findByJlptLevel(level: Int, pageable: Pageable): Page<VocabularyEntity>

    fun findByItContextTrue(pageable: Pageable): Page<VocabularyEntity>

    @Query("SELECT v FROM VocabularyEntity v WHERE v.lesson.lessonId = :lessonId")
    fun findByLessonId(@Param("lessonId") lessonId: UUID, pageable: Pageable): Page<VocabularyEntity>
}