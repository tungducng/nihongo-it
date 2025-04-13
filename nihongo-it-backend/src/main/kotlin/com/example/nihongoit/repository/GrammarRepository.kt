package com.example.nihongoit.repository

import com.example.nihongoit.entity.GrammarEntity
import com.example.nihongoit.entity.JlptLevel
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.stereotype.Repository
import java.util.UUID

@Repository
interface GrammarRepository : JpaRepository<GrammarEntity, UUID> {
    fun findByJlptLevel(jlptLevel: JlptLevel): List<GrammarEntity>
    fun findByPatternContainingIgnoreCase(pattern: String): List<GrammarEntity>
    fun findByJlptLevelAndDifficultyLevel(jlptLevel: JlptLevel, difficultyLevel: Int): List<GrammarEntity>
} 