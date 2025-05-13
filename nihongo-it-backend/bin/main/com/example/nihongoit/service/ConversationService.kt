package com.example.nihongoit.service

import com.example.nihongoit.dto.ConversationDTO
import com.example.nihongoit.dto.CreateConversationRequest
import com.example.nihongoit.dto.PagedResponse
import com.example.nihongoit.dto.UpdateConversationRequest
import org.springframework.data.domain.Pageable
import java.util.UUID

interface ConversationService {
    /**
     * Get all conversations with pagination
     */
    fun getAllConversations(pageable: Pageable): PagedResponse<ConversationDTO>
    
    /**
     * Search conversations by title or description
     */
    fun searchConversations(query: String, pageable: Pageable): PagedResponse<ConversationDTO>
    
    /**
     * Get conversations by JLPT level
     */
    fun getConversationsByJlptLevel(level: String, pageable: Pageable): PagedResponse<ConversationDTO>
    
    /**
     * Get conversation by ID
     */
    fun getConversationById(conversationId: UUID): ConversationDTO
    
    /**
     * Create a new conversation
     */
    fun createConversation(request: CreateConversationRequest): ConversationDTO
    
    /**
     * Update an existing conversation
     */
    fun updateConversation(conversationId: UUID, request: UpdateConversationRequest): ConversationDTO
    
    /**
     * Delete a conversation
     */
    fun deleteConversation(conversationId: UUID)
} 