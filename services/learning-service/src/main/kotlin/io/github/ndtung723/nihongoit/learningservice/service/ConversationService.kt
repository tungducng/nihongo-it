package io.github.ndtung723.nihongoit.learningservice.service

import io.github.ndtung723.nihongoit.common.exception.BusinessException
import io.github.ndtung723.nihongoit.learningservice.dto.ConversationDTO
import io.github.ndtung723.nihongoit.learningservice.dto.CreateConversationRequest
import io.github.ndtung723.nihongoit.learningservice.dto.PagedResponse
import io.github.ndtung723.nihongoit.learningservice.dto.UpdateConversationRequest
import io.github.ndtung723.nihongoit.learningservice.entity.ConversationEntity
import io.github.ndtung723.nihongoit.learningservice.entity.ConversationLineEntity
import io.github.ndtung723.nihongoit.learningservice.repository.ConversationLineRepository
import io.github.ndtung723.nihongoit.learningservice.repository.ConversationRepository
import org.springframework.data.domain.Pageable
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import java.util.UUID

@Service
class ConversationService(
    private val conversationRepository: ConversationRepository,
    private val conversationLineRepository: ConversationLineRepository,
) {
    fun getAllConversations(pageable: Pageable): PagedResponse<ConversationDTO> {
        val page = conversationRepository.findAll(pageable)
        val content = page.content.map { ConversationDTO.fromEntity(it) }

        return PagedResponse(
            content = content,
            page = page.number,
            size = page.size,
            totalElements = page.totalElements,
            totalPages = page.totalPages,
            lastPage = page.isLast,
        )
    }

    fun searchConversations(
        query: String,
        pageable: Pageable,
    ): PagedResponse<ConversationDTO> {
        val page = conversationRepository.findByTitleContainingIgnoreCaseOrDescriptionContainingIgnoreCase(query, query, pageable)
        val content = page.content.map { ConversationDTO.fromEntity(it) }

        return PagedResponse(
            content = content,
            page = page.number,
            size = page.size,
            totalElements = page.totalElements,
            totalPages = page.totalPages,
            lastPage = page.isLast,
        )
    }

    fun getConversationsByJlptLevel(
        level: String,
        pageable: Pageable,
    ): PagedResponse<ConversationDTO> {
        val page = conversationRepository.findByJlptLevel(level, pageable)
        val content = page.content.map { ConversationDTO.fromEntity(it) }

        return PagedResponse(
            content = content,
            page = page.number,
            size = page.size,
            totalElements = page.totalElements,
            totalPages = page.totalPages,
            lastPage = page.isLast,
        )
    }

    fun getConversationById(conversationId: UUID): ConversationDTO {
        val conversation =
            conversationRepository
                .findById(conversationId)
                .orElseThrow { BusinessException("Conversation not found with id: $conversationId") }

        return ConversationDTO.fromEntity(conversation)
    }

    @Transactional
    fun createConversation(request: CreateConversationRequest): ConversationDTO {
        val conversation =
            ConversationEntity(
                title = request.title,
                description = request.description,
                jlptLevel = request.jlptLevel,
                unit = request.unit,
            )

        val savedConversation = conversationRepository.save(conversation)

        // Save conversation lines
        if (request.lines.isNotEmpty()) {
            val conversationLines =
                request.lines.map { lineRequest ->
                    ConversationLineEntity(
                        conversation = savedConversation,
                        speaker = lineRequest.speaker,
                        japaneseText = lineRequest.japaneseText,
                        vietnameseTranslation = lineRequest.vietnameseTranslation,
                        notes = lineRequest.notes,
                        importantVocab = lineRequest.importantVocab,
                        orderIndex = lineRequest.orderIndex,
                    )
                }

            conversationLineRepository.saveAll(conversationLines)
        }

        return getConversationById(requireNotNull(savedConversation.convId) { "Conversation ID missing after save" })
    }

    @Transactional
    fun updateConversation(
        conversationId: UUID,
        request: UpdateConversationRequest,
    ): ConversationDTO {
        val conversation =
            conversationRepository
                .findById(conversationId)
                .orElseThrow { BusinessException("Conversation not found with id: $conversationId") }

        // Update conversation properties (updatedAt auto-filled by JPA auditing)
        conversation.apply {
            title = request.title ?: title
            description = request.description ?: description
            jlptLevel = request.jlptLevel ?: jlptLevel
            unit = request.unit ?: unit
        }

        conversationRepository.save(conversation)

        // Update conversation lines if provided
        if (request.lines != null) {
            // Delete existing lines
            conversationLineRepository.deleteByConversationId(conversationId)

            // Create new lines
            val conversationLines =
                request.lines.map { lineRequest ->
                    ConversationLineEntity(
                        conversation = conversation,
                        speaker = lineRequest.speaker,
                        japaneseText = lineRequest.japaneseText,
                        vietnameseTranslation = lineRequest.vietnameseTranslation,
                        notes = lineRequest.notes,
                        importantVocab = lineRequest.importantVocab,
                        orderIndex = lineRequest.orderIndex,
                    )
                }

            conversationLineRepository.saveAll(conversationLines)
        }

        return getConversationById(conversationId)
    }

    @Transactional
    fun deleteConversation(conversationId: UUID) {
        if (!conversationRepository.existsById(conversationId)) {
            throw BusinessException("Conversation not found with id: $conversationId")
        }

        // Delete associated lines first
        conversationLineRepository.deleteByConversationId(conversationId)

        // Delete the conversation
        conversationRepository.deleteById(conversationId)
    }
}
