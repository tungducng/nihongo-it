package com.example.nihongoit.service

import com.example.nihongoit.dto.CreateTopicRequest
import com.example.nihongoit.dto.TopicDTO
import com.example.nihongoit.dto.UpdateTopicRequest
import com.example.nihongoit.dto.toDTO
import com.example.nihongoit.entity.JlptLevel
import com.example.nihongoit.entity.TopicEntity
import com.example.nihongoit.exception.BusinessException
import com.example.nihongoit.repository.CategoryRepository
import com.example.nihongoit.repository.TopicRepository
import com.example.nihongoit.repository.UserRepository
import com.example.nihongoit.util.UserAuthUtil
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import java.time.LocalDateTime
import java.util.UUID

@Service
class TopicService(
    private val topicRepository: TopicRepository,
    private val categoryRepository: CategoryRepository,
    private val userRepository: UserRepository,
    private val userAuthUtil: UserAuthUtil
)  {

    @Transactional(readOnly = true)
    fun getAllTopics(): List<TopicDTO> {
        return topicRepository.findAll().map { it.toDTO() }
    }

    @Transactional(readOnly = true)
    fun getTopicsByCategoryId(categoryId: UUID): List<TopicDTO> {
        val topics = topicRepository.findByCategoryCategoryId(categoryId)
        return topics.map { it.toDTO() }
    }

    @Transactional(readOnly = true)
    fun getTopicById(topicId: UUID): TopicDTO {
        val topic = topicRepository.findById(topicId)
            .orElseThrow { BusinessException("Topic not found with ID: $topicId") }

        return topic.toDTO()
    }

    @Transactional
    fun createTopic(request: CreateTopicRequest): TopicDTO {
        val currentUserId = userAuthUtil.getCurrentUserId()
            ?: throw BusinessException("User not authenticated")

        val category = categoryRepository.findById(request.categoryId)
            .orElseThrow { BusinessException("Category not found with ID: ${request.categoryId}") }

        // Check if a topic with the same name already exists in this category
        if (topicRepository.existsByNameAndCategory(request.name, category)) {
            throw BusinessException("A topic with the name '${request.name}' already exists in this category")
        }

        val user = userRepository.findById(currentUserId)
            .orElseThrow { BusinessException("User not found") }

        val topic = TopicEntity(
            name = request.name,
            meaning = request.meaning,
            displayOrder = request.displayOrder,
            category = category,
            createdAt = LocalDateTime.now(),
            updatedAt = LocalDateTime.now()
        )

        val savedTopic = topicRepository.save(topic)
        return savedTopic.toDTO()
    }

    @Transactional
    fun updateTopic(topicId: UUID, request: UpdateTopicRequest): TopicDTO {
        val topic = topicRepository.findById(topicId)
            .orElseThrow { BusinessException("Topic not found with ID: $topicId") }

        // Handle category change if needed
        val category = if (request.categoryId != null && request.categoryId != topic.category.categoryId) {
            categoryRepository.findById(request.categoryId)
                .orElseThrow { BusinessException("Category not found with ID: ${request.categoryId}") }
        } else {
            topic.category
        }

        // Check for duplicate name if name is being changed and category remains the same
        if (request.name != null &&
            request.name != topic.name &&
            request.categoryId == topic.category.categoryId &&
            topicRepository.existsByNameAndCategory(request.name, topic.category)) {
            throw BusinessException("A topic with the name '${request.name}' already exists in this category")
        }

        val updatedTopic = topic.copy(
            name = request.name ?: topic.name,
            meaning = request.meaning ?: topic.meaning,
            displayOrder = request.displayOrder ?: topic.displayOrder,
            category = category,
            updatedAt = LocalDateTime.now()
        )

        val savedTopic = topicRepository.save(updatedTopic)
        return savedTopic.toDTO()
    }

    @Transactional
    fun deleteTopic(topicId: UUID) {
        if (!topicRepository.existsById(topicId)) {
            throw BusinessException("Topic not found with ID: $topicId")
        }

        // Note: Due to cascade.ALL and orphanRemoval=true on the vocabularyItems relationship,
        // deleting a topic will also delete all associated vocabulary items
        topicRepository.deleteById(topicId)
    }

    @Transactional(readOnly = true)
    fun searchTopicsInCategory(categoryId: UUID, query: String): List<TopicDTO> {
        val category = categoryRepository.findById(categoryId)
            .orElseThrow { BusinessException("Category not found with ID: $categoryId") }

        val topics = topicRepository.findByCategoryAndNameContainingIgnoreCase(category, query)
        return topics.map { it.toDTO() }
    }
}