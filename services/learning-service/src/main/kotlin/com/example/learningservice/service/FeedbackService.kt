package com.example.learningservice.service

import com.example.learningservice.dto.FeedbackDTO
import com.example.learningservice.entity.FeedbackEntity
import com.example.learningservice.repository.FeedbackRepository
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import java.time.LocalDateTime

@Service
class FeedbackService(
    private val feedbackRepository: FeedbackRepository,
) {
    @Transactional
    fun saveFeedback(feedbackDTO: FeedbackDTO): FeedbackDTO {
        // userId is a cross-service reference (no local users table to validate against).
        val feedbackEntity =
            FeedbackEntity(
                feedbackId = null,
                userId = feedbackDTO.userId,
                contentType = feedbackDTO.contentType,
                contentId = feedbackDTO.contentId,
                content = feedbackDTO.content,
                createdAt = LocalDateTime.now(),
            )

        val savedEntity = feedbackRepository.save(feedbackEntity)

        return FeedbackDTO(
            feedbackId = savedEntity.feedbackId,
            userId = savedEntity.userId,
            contentType = savedEntity.contentType,
            contentId = savedEntity.contentId,
            content = savedEntity.content,
            createdAt = savedEntity.createdAt,
        )
    }
}
