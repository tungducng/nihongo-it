package com.example.nihongoit.repository

import com.example.nihongoit.entity.ConversationEntity
import com.example.nihongoit.entity.JlptLevel
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.stereotype.Repository
import java.util.UUID

@Repository
interface ConversationRepository : JpaRepository<ConversationEntity, UUID> {
    fun findByJlptLevel(jlptLevel: JlptLevel): List<ConversationEntity>
    fun findByItContextIgnoreCase(itContext: String): List<ConversationEntity>
    fun findByTitleContainingIgnoreCase(title: String): List<ConversationEntity>
    fun findByIsActiveTrue(): List<ConversationEntity>
} 