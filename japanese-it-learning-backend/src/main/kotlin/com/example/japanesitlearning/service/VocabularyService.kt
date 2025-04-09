package com.example.japanesitlearning.service

import com.example.japanesitlearning.dto.ResponseDto
import com.example.japanesitlearning.dto.ResponseType
import com.example.japanesitlearning.dto.vocabulary.CreateVocabularyRequestDto
import com.example.japanesitlearning.dto.vocabulary.CreateVocabularyResponseDto
import com.example.japanesitlearning.dto.vocabulary.GetVocabularyResponseDto
import com.example.japanesitlearning.dto.vocabulary.PagedVocabularyResponseDto
import com.example.japanesitlearning.dto.vocabulary.UpdateVocabularyRequestDto
import com.example.japanesitlearning.dto.vocabulary.UpdateVocabularyResponseDto
import com.example.japanesitlearning.dto.vocabulary.VocabularyDto
import com.example.japanesitlearning.dto.vocabulary.VocabularyFilterRequestDto
import com.example.japanesitlearning.entity.JLPTLevel
import com.example.japanesitlearning.entity.VocabularyEntity
import com.example.japanesitlearning.exception.BusinessException
import com.example.japanesitlearning.repository.UserRepository
import com.example.japanesitlearning.repository.VocabularyRepository
import com.example.japanesitlearning.security.UserAuthUtil
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

        val vocabulary = VocabularyEntity(
            hiragana = request.hiragana,
            kanji = request.kanji,
            katakana = request.katakana,
            meaning = request.meaning,
            exampleSentence = request.exampleSentence,
            audioPath = request.audioPath,
            category = request.category,
            jlptLevel = JLPTLevel.valueOf(request.jlptLevel),
            contentType = request.contentType,
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
            filter.contentType != null -> {
                vocabularyRepository.findByContentType(filter.contentType, pageable)
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

        if (vocabulary.createdBy.userId != currentUserId) {
            throw BusinessException("You can only update vocabulary you created")
        }

        val updatedVocabulary = vocabulary.copy(
            hiragana = request.hiragana,
            kanji = request.kanji,
            katakana = request.katakana,
            meaning = request.meaning,
            exampleSentence = request.exampleSentence,
            audioPath = request.audioPath,
            category = request.category,
            jlptLevel = JLPTLevel.valueOf(request.jlptLevel),
            contentType = request.contentType,
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

        if (vocabulary.createdBy.userId != currentUserId) {
            throw BusinessException("You can only delete vocabulary you created")
        }

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

        val pageable = PageRequest.of(filter.page, filter.size)
        val result = vocabularyRepository.findSavedByUser(currentUserId, pageable)

        val content = result.content.map { vocabulary ->
            mapToResponse(vocabulary, true)
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

    private fun mapToResponse(vocabulary: VocabularyEntity, isSaved: Boolean = false): VocabularyDto {
        return VocabularyDto(
            vocabId = vocabulary.vocabId!!,
            hiragana = vocabulary.hiragana,
            kanji = vocabulary.kanji,
            katakana = vocabulary.katakana,
            meaning = vocabulary.meaning,
            exampleSentence = vocabulary.exampleSentence,
            audioPath = vocabulary.audioPath,
            category = vocabulary.category,
            jlptLevel = vocabulary.jlptLevel,
            contentType = vocabulary.contentType,
            createdAt = vocabulary.createdAt,
            createdBy = vocabulary.createdBy.email,
            isSaved = isSaved,
        )
    }
}
