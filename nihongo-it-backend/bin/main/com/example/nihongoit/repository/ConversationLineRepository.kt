package com.example.nihongoit.repository

import com.example.nihongoit.entity.ConversationLineEntity
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.jpa.repository.Modifying
import org.springframework.data.jpa.repository.Query
import org.springframework.stereotype.Repository
import java.util.UUID

@Repository
interface ConversationLineRepository : JpaRepository<ConversationLineEntity, UUID> {
    /**
     * Find lines by conversation ID
     */
    fun findByConversationConvId(conversationId: UUID): List<ConversationLineEntity>
    
    /**
     * Find lines by conversation ID and order them by orderIndex
     */
    fun findByConversationConvIdOrderByOrderIndex(conversationId: UUID): List<ConversationLineEntity>
    
    /**
     * Delete lines by conversation ID
     */
    @Modifying
    @Query("DELETE FROM ConversationLineEntity c WHERE c.conversation.convId = :conversationId")
    fun deleteByConversationId(conversationId: UUID)
} 