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
import com.example.nihongoit.util.UserAuthUtil
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
    private val userAuthUtil: UserAuthUtil,
) {

    @Transactional
    fun createVocabulary(request: CreateVocabularyRequestDto): CreateVocabularyResponseDto {
        val currentUserId = userAuthUtil.getCurrentUserId()
            ?: throw BusinessException("User not authenticated")

        val user = userRepository.findById(currentUserId)
            .orElseThrow { BusinessException("User not found") }


        // Check if at least one of hiragana, kanji, or katakana is provided
        if (request.hiragana.isNullOrBlank() && request.kanji.isNullOrBlank() && request.katakana.isNullOrBlank()) {
            return CreateVocabularyResponseDto(
                result = ResponseDto(
                    status = ResponseType.NG,
                    message = "At least one of hiragana, kanji, or katakana must be provided",
                ),
            )
        }

        val vocabulary = VocabularyEntity(
            hiragana = request.hiragana,
            kanji = request.kanji,
            katakana = request.katakana,
            meaning = request.meaning,
            exampleSentence = request.exampleSentence,
            exampleSentenceTranslation = request.exampleSentenceTranslation,
            audioPath = request.audioPath,
            category = request.category,
            jlptLevel = JlptLevel.valueOf(request.jlptLevel),
            createdBy = user,
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
    fun getVocabulary(vocabId: UUID): GetVocabularyResponseDto {
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
    fun filterVocabulary(filter: VocabularyFilterRequestDto): PagedVocabularyResponseDto {
        val pageable = PageRequest.of(filter.page, filter.size)
        val currentUserId = userAuthUtil.getCurrentUserId()

        val result: Page<VocabularyEntity> = when {
            filter.keyword != null -> {
                vocabularyRepository.searchVocabulary(filter.keyword, pageable)
            }
            filter.jlptLevel != null -> {
                vocabularyRepository.findByJlptLevel(filter.jlptLevel, pageable)
            }
            filter.category != null -> {
                vocabularyRepository.findByCategory(filter.category, pageable)
            }
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
    fun updateVocabulary(vocabId: UUID, request: UpdateVocabularyRequestDto): UpdateVocabularyResponseDto {
        val currentUserId = userAuthUtil.getCurrentUserId()
            ?: throw BusinessException("User not authenticated")

        val vocabulary = vocabularyRepository.findById(vocabId)
            .orElseThrow { BusinessException("Vocabulary not found") }

//        if (vocabulary.createdBy?.userId != currentUserId) {
//            throw BusinessException("You can only update vocabulary you created")
//        }

        val updatedVocabulary = vocabulary.copy(
            hiragana = request.hiragana,
            kanji = request.kanji,
            katakana = request.katakana,
            meaning = request.meaning,
            exampleSentence = request.exampleSentence,
            exampleSentenceTranslation = request.exampleSentenceTranslation,
            audioPath = request.audioPath,
            category = request.category,
            jlptLevel = JlptLevel.valueOf(request.jlptLevel),
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

    @Transactional
    fun deleteVocabulary(vocabId: UUID): ResponseDto {
        val currentUserId = userAuthUtil.getCurrentUserId()
            ?: throw BusinessException("User not authenticated")

        val vocabulary = vocabularyRepository.findById(vocabId)
            .orElseThrow { BusinessException("Vocabulary not found") }

//        if (vocabulary.createdBy.userId != currentUserId) {
//            throw BusinessException("You can only delete vocabulary you created")
//        }

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
    
    /**
     * Creates a PageRequest with sorting based on the sort parameter
     */
    private fun createPageableWithSort(page: Int, size: Int, sort: String?): PageRequest {
        return when(sort) {
            "date_asc" -> PageRequest.of(page, size, org.springframework.data.domain.Sort.by("createdAt").ascending())
            "date_desc" -> PageRequest.of(page, size, org.springframework.data.domain.Sort.by("createdAt").descending())
            "jlpt_asc" -> PageRequest.of(page, size, org.springframework.data.domain.Sort.by("jlptLevel").ascending())
            "jlpt_desc" -> PageRequest.of(page, size, org.springframework.data.domain.Sort.by("jlptLevel").descending())
            "alpha_asc" -> PageRequest.of(page, size, org.springframework.data.domain.Sort.by("hiragana").ascending())
            "alpha_desc" -> PageRequest.of(page, size, org.springframework.data.domain.Sort.by("hiragana").descending())
            else -> PageRequest.of(page, size, org.springframework.data.domain.Sort.by("createdAt").descending())
        }
    }

    private fun mapToResponse(vocabulary: VocabularyEntity, isSaved: Boolean = false): VocabularyDto {
        return VocabularyDto(
            vocabId = vocabulary.vocabId!!,
            hiragana = vocabulary.hiragana,
            kanji = vocabulary.kanji,
            katakana = vocabulary.katakana,
            meaning = vocabulary.meaning,
            exampleSentence = vocabulary.exampleSentence,
            exampleSentenceTranslation = vocabulary.exampleSentenceTranslation,
            audioPath = vocabulary.audioPath,
            category = vocabulary.category,
            jlptLevel = vocabulary.jlptLevel,
            createdAt = vocabulary.createdAt,
            createdBy = vocabulary.createdBy?.email,
            isSaved = isSaved,
        )
    }
}
