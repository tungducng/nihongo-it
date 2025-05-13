package com.example.nihongoit.service

import com.example.nihongoit.dto.ResponseDto
import com.example.nihongoit.dto.ResponseType
import com.example.nihongoit.dto.vocabulary.CreateVocabularyRequestDto
import com.example.nihongoit.dto.vocabulary.CreateVocabularyResponseDto
import com.example.nihongoit.dto.vocabulary.GetVocabularyResponseDto
import com.example.nihongoit.dto.vocabulary.PagedVocabularyResponseDto
import com.example.nihongoit.dto.vocabulary.UpdateVocabularyRequestDto
import com.example.nihongoit.dto.vocabulary.UpdateVocabularyResponseDto
import com.example.nihongoit.dto.vocabulary.VocabularyDto
import com.example.nihongoit.dto.vocabulary.VocabularyFilterRequestDto
import com.example.nihongoit.entity.JlptLevel
import com.example.nihongoit.entity.VocabularyEntity
import com.example.nihongoit.exception.BusinessException
import com.example.nihongoit.repository.UserRepository
import com.example.nihongoit.repository.VocabularyRepository
import com.example.nihongoit.repository.TopicRepository
import com.example.nihongoit.repository.CategoryRepository
import com.example.nihongoit.repository.FlashcardRepository
import com.example.nihongoit.util.UserAuthUtil
import org.slf4j.LoggerFactory
import org.springframework.data.domain.Page
import org.springframework.data.domain.PageRequest
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import java.time.LocalDateTime
import java.util.*

@Service
class VocabularyService(
    private val vocabularyRepository: VocabularyRepository,
    private val userRepository: UserRepository,
    private val topicRepository: TopicRepository,
    private val categoryRepository: CategoryRepository,
    private val userAuthUtil: UserAuthUtil,
    private val flashcardService: FlashcardService,
    private val flashcardRepository: FlashcardRepository,
) {
    private val logger = LoggerFactory.getLogger(VocabularyService::class.java)

    @Transactional
    fun createVocabulary(request: CreateVocabularyRequestDto): CreateVocabularyResponseDto {
        val currentUserId = userAuthUtil.getCurrentUserId()
            ?: throw BusinessException("User not authenticated")

        val user = userRepository.findById(currentUserId)
            .orElseThrow { BusinessException("User not found") }

        // Check if term is provided
        if (request.term.isBlank()) {
            return CreateVocabularyResponseDto(
                result = ResponseDto(
                    status = ResponseType.NG,
                    message = "Term must be provided",
                ),
            )
        }

        // Check if vocabulary with this term already exists
        if (vocabularyRepository.existsByTerm(request.term)) {
            return CreateVocabularyResponseDto(
                result = ResponseDto(
                    status = ResponseType.NG,
                    message = "Vocabulary with term '${request.term}' already exists",
                ),
            )
        }

        // Find topic by name - now required
        val topic = request.topicName.let {
            val topics = topicRepository.findByName(it)
            if (topics.isEmpty()) {
                return CreateVocabularyResponseDto(
                    result = ResponseDto(
                        status = ResponseType.NG,
                        message = "Topic '${request.topicName}' does not exist",
                    ),
                )
            } else {
                topics.first()
            }
        }

        val vocabulary = VocabularyEntity(
            term = request.term,
            meaning = request.meaning,
            pronunciation = request.pronunciation,
            example = request.example,
            exampleMeaning = request.exampleMeaning,
            audioPath = request.audioPath,
            jlptLevel = JlptLevel.valueOf(request.jlptLevel),
            topic = topic,
            createdAt = LocalDateTime.now(),
        )

        vocabularyRepository.save(vocabulary)
        return CreateVocabularyResponseDto(
            result = ResponseDto(
                status = ResponseType.OK,
                message = "Vocabulary created successfully",
            ),
        )
    }

    @Transactional(readOnly = true)
    fun getVocabularybyId(vocabId: UUID): GetVocabularyResponseDto {
        val currentUserId = userAuthUtil.getCurrentUserId()
        val vocabulary = vocabularyRepository.findById(vocabId)
            .orElseThrow { BusinessException("Vocabulary not found") }

        val isSaved = currentUserId?.let { userId ->
            vocabulary.savedByUsers.any { it.userId == userId }
        } ?: false

        return GetVocabularyResponseDto(
            result = ResponseDto(
                status = ResponseType.OK,
                message = "Vocabulary retrieved successfully",
            ),
            data = mapToResponse(vocabulary, isSaved),
        )
    }

    @Transactional(readOnly = true)
    fun getVocabularyByTerm(term: String): GetVocabularyResponseDto {
        val currentUserId = userAuthUtil.getCurrentUserId()
        val vocabulary = vocabularyRepository.findByTerm(term)
            .orElseThrow { BusinessException("Vocabulary not found with term: $term") }

        val isSaved = currentUserId?.let { userId ->
            vocabulary.savedByUsers.any { it.userId == userId }
        } ?: false

        return GetVocabularyResponseDto(
            result = ResponseDto(
                status = ResponseType.OK,
                message = "Vocabulary retrieved successfully",
            ),
            data = mapToResponse(vocabulary, isSaved),
        )
    }

    @Transactional
    fun updateVocabulary(vocabId: UUID, request: UpdateVocabularyRequestDto): UpdateVocabularyResponseDto {
        val currentUserId = userAuthUtil.getCurrentUserId()
            ?: throw BusinessException("User not authenticated")

        val vocabulary = vocabularyRepository.findById(vocabId)
            .orElseThrow { BusinessException("Vocabulary not found") }

        // Check if the new term is blank
        if (request.term.isBlank()) {
            return UpdateVocabularyResponseDto(
                result = ResponseDto(
                    status = ResponseType.NG,
                    message = "Term cannot be blank"
                ),
                data = mapToResponse(vocabulary)
            )
        }

        // Check if the term is being updated and if the new term already exists
        if (request.term != vocabulary.term && vocabularyRepository.existsByTerm(request.term)) {
            return UpdateVocabularyResponseDto(
                result = ResponseDto(
                    status = ResponseType.NG,
                    message = "Vocabulary with term '${request.term}' already exists"
                ),
                data = mapToResponse(vocabulary)
            )
        }

        // Find topic by name if provided - now required to exist
        val topic = request.topicName?.let {
            val topics = topicRepository.findByName(it)
            if (topics.isEmpty()) {
                return UpdateVocabularyResponseDto(
                    result = ResponseDto(
                        status = ResponseType.NG,
                        message = "Topic '${request.topicName}' does not exist",
                    ),
                    data = mapToResponse(vocabulary),
                )
            } else {
                topics.first()
            }
        } ?: vocabulary.topic

        val updatedVocabulary = vocabulary.copy(
            term = request.term,
            meaning = request.meaning,
            pronunciation = request.pronunciation,
            example = request.example,
            exampleMeaning = request.exampleMeaning,
            audioPath = request.audioPath,
            jlptLevel = JlptLevel.valueOf(request.jlptLevel),
            topic = topic
        )

        val savedVocab = vocabularyRepository.save(updatedVocabulary)
        return UpdateVocabularyResponseDto(
            result = ResponseDto(
                status = ResponseType.OK,
                message = "Vocabulary updated successfully",
            ),
            data = mapToResponse(savedVocab),
        )
    }

    @Transactional(readOnly = true)
    fun filterVocabulary(filter: VocabularyFilterRequestDto): PagedVocabularyResponseDto {
        val pageable = PageRequest.of(filter.page, filter.size)
        val currentUserId = userAuthUtil.getCurrentUserId()

        val result: Page<VocabularyEntity> = when {
            // Kết hợp tìm kiếm theo cả topicId và jlptLevel nếu cả hai đều được cung cấp
            filter.topicId != null && filter.jlptLevel != null -> {
                vocabularyRepository.findByTopic_TopicIdAndJlptLevel(filter.topicId, filter.jlptLevel, pageable)
            }
            // Tìm kiếm theo keyword
            filter.keyword != null -> {
                vocabularyRepository.searchVocabulary(filter.keyword, pageable)
            }
            // Tìm kiếm theo jlptLevel
            filter.jlptLevel != null -> {
                vocabularyRepository.findByJlptLevel(filter.jlptLevel, pageable)
            }
            // Tìm kiếm theo topicId
            filter.topicId != null -> {
                vocabularyRepository.findByTopic_TopicId(filter.topicId, pageable)
            }
            // Tìm kiếm theo topicName
            filter.topicName != null -> {
                vocabularyRepository.findByTopicName(filter.topicName, pageable)
            }
            // Lấy tất cả
            else -> {
                vocabularyRepository.findAll(pageable)
            }
        }

        val content = result.content.map { vocabulary ->
            val isSaved = currentUserId?.let { userId ->
                vocabulary.savedByUsers.any { it.userId == userId }
            } ?: false

            mapToResponse(vocabulary, isSaved)
        }

        return PagedVocabularyResponseDto(
            content = content,
            page = result.number,
            size = result.size,
            totalElements = result.totalElements,
            totalPages = result.totalPages,
            lastPage = result.isLast,
        )
    }

    @Transactional
    fun deleteVocabulary(vocabId: UUID): ResponseDto {
        val currentUserId = userAuthUtil.getCurrentUserId()
            ?: throw BusinessException("User not authenticated")

        val vocabulary = vocabularyRepository.findById(vocabId)
            .orElseThrow { BusinessException("Vocabulary not found") }

        vocabularyRepository.delete(vocabulary)

        return ResponseDto(
            status = ResponseType.OK,
            message = "Vocabulary deleted successfully",
        )
    }

    @Transactional
    fun saveVocabularyToNotebook(vocabId: UUID): VocabularyDto {
        val currentUserId = userAuthUtil.getCurrentUserId()
            ?: throw BusinessException("User not authenticated")

        val user = userRepository.findById(currentUserId)
            .orElseThrow { BusinessException("User not found") }

        val vocabulary = vocabularyRepository.findById(vocabId)
            .orElseThrow { BusinessException("Vocabulary not found") }

        vocabulary.savedByUsers.add(user)
        vocabularyRepository.save(vocabulary)

        // Automatically create a flashcard for this vocabulary
        try {
            flashcardService.createFlashcardFromVocabulary(vocabId)
            logger.info("Automatically created flashcard for vocabulary $vocabId when saved to notebook")
        } catch (e: Exception) {
            // Log the error but don't fail the save operation
            // This allows vocabulary to be saved even if flashcard creation fails
            // (e.g., if a flashcard already exists)
            logger.warn("Failed to auto-create flashcard for vocabulary $vocabId: ${e.message}")
        }

        return mapToResponse(vocabulary, true)
    }

    @Transactional
    fun removeVocabularyFromNotebook(vocabId: UUID): VocabularyDto {
        val currentUserId = userAuthUtil.getCurrentUserId()
            ?: throw BusinessException("User not authenticated")

        val vocabulary = vocabularyRepository.findById(vocabId)
            .orElseThrow { BusinessException("Vocabulary not found") }

        vocabulary.savedByUsers.removeIf { it.userId == currentUserId }
        vocabularyRepository.save(vocabulary)

        // Delete associated flashcard for this vocabulary
        try {
            // Find the flashcards for this vocabulary and user
            val flashcards = flashcardRepository.findByUser_UserIdAndVocabulary_VocabId(currentUserId, vocabId)
            
            // Delete each flashcard
            flashcards.forEach { flashcard ->
                flashcardService.deleteFlashcard(flashcard.flashcardId!!)
                logger.info("Deleted flashcard ${flashcard.flashcardId} when vocabulary $vocabId was removed from notebook")
            }
        } catch (e: Exception) {
            // Log the error but don't fail the remove operation
            logger.warn("Failed to delete flashcard for vocabulary $vocabId: ${e.message}")
        }

        return mapToResponse(vocabulary, false)
    }

    @Transactional(readOnly = true)
    fun getSavedVocabulary(filter: VocabularyFilterRequestDto): PagedVocabularyResponseDto {
        val currentUserId = userAuthUtil.getCurrentUserId()
            ?: throw BusinessException("User not authenticated")

        // Use pagination parameters from the filter
        val pageable = createPageableWithSort(filter.page, filter.size, filter.sort)

        // Apply keyword filter if provided
        val result = if (filter.keyword != null && filter.keyword.isNotBlank()) {
            vocabularyRepository.findSavedByUserAndKeyword(currentUserId, filter.keyword, pageable)
        } else {
            vocabularyRepository.findSavedByUser(currentUserId, pageable)
        }

        val content = result.content.map { vocabulary ->
            mapToResponse(vocabulary, true)
        }

        // Create a response with pagination information
        return PagedVocabularyResponseDto(
            content = content,
            page = result.number,
            size = result.size,
            totalElements = result.totalElements,
            totalPages = result.totalPages,
            lastPage = result.isLast,
        )
    }

    @Transactional(readOnly = true)
    fun filterVocabularyByTopicId(filter: VocabularyFilterRequestDto): PagedVocabularyResponseDto {
        val pageable = PageRequest.of(filter.page, filter.size)
        val currentUserId = userAuthUtil.getCurrentUserId()

        if (filter.topicId == null) {
            throw BusinessException("Topic ID is required")
        }

        // Kiểm tra topic tồn tại
        val topic = topicRepository.findById(filter.topicId)
            .orElseThrow { BusinessException("Topic not found with ID: ${filter.topicId}") }

        // Tìm vocabulary theo topic ID kết hợp với keyword nếu có
        val result = if (filter.keyword != null) {
            // Tìm theo topic ID và keyword
            vocabularyRepository.findByTopic_TopicIdAndTermContainingIgnoreCaseOrTopic_TopicIdAndMeaningContainingIgnoreCase(
                filter.topicId, filter.keyword, filter.topicId, filter.keyword, pageable
            )
        } else {
            // Chỉ tìm theo topic ID
            vocabularyRepository.findByTopic_TopicId(filter.topicId, pageable)
        }

        val content = result.content.map { vocabulary ->
            val isSaved = currentUserId?.let { userId ->
                vocabulary.savedByUsers.any { it.userId == userId }
            } ?: false

            mapToResponse(vocabulary, isSaved)
        }

        return PagedVocabularyResponseDto(
            content = content,
            page = result.number,
            size = result.size,
            totalElements = result.totalElements,
            totalPages = result.totalPages,
            lastPage = result.isLast,
        )
    }

    /**
     * Creates a PageRequest with sorting based on the sort parameter
     */
    private fun createPageableWithSort(page: Int, size: Int, sort: String?): PageRequest {
        return when(sort) {
            "date_asc" -> PageRequest.of(page, size, org.springframework.data.domain.Sort.by("createdAt").ascending())
            "date_desc" -> PageRequest.of(page, size, org.springframework.data.domain.Sort.by("createdAt").descending())
            "jlpt_asc" -> PageRequest.of(page, size, org.springframework.data.domain.Sort.by("jlptLevel").ascending())
            "jlpt_desc" -> PageRequest.of(page, size, org.springframework.data.domain.Sort.by("jlptLevel").descending())
            "alpha_asc" -> PageRequest.of(page, size, org.springframework.data.domain.Sort.by("term").ascending())
            "alpha_desc" -> PageRequest.of(page, size, org.springframework.data.domain.Sort.by("term").descending())
            else -> PageRequest.of(page, size, org.springframework.data.domain.Sort.by("createdAt").descending())
        }
    }

    private fun mapToResponse(vocabulary: VocabularyEntity, isSaved: Boolean = false): VocabularyDto {
        return VocabularyDto(
            vocabId = vocabulary.vocabId!!,
            term = vocabulary.term ?: "",
            meaning = vocabulary.meaning,
            pronunciation = vocabulary.pronunciation,
            example = vocabulary.example,
            exampleMeaning = vocabulary.exampleMeaning,
            audioPath = vocabulary.audioPath,
            jlptLevel = vocabulary.jlptLevel,
            topicId = vocabulary.topic.topicId,
            topicName = vocabulary.topic.name,
            createdAt = vocabulary.createdAt,
            isSaved = isSaved,
        )
    }

    /**
     * Get total count of vocabulary entries
     */
    fun getVocabularyCount(): Int {
        return vocabularyRepository.count().toInt()
    }
}