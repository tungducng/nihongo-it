package io.github.ndtung723.nihongoit.learningservice.service

import io.github.ndtung723.nihongoit.common.exception.BusinessException
import io.github.ndtung723.nihongoit.common.result.ServiceResult
import io.github.ndtung723.nihongoit.learningservice.dto.CreateFlashcardRequestDto
import io.github.ndtung723.nihongoit.learningservice.dto.CreateFlashcardResponseDto
import io.github.ndtung723.nihongoit.learningservice.dto.DeleteFlashcardResponseDto
import io.github.ndtung723.nihongoit.learningservice.dto.FlashcardDTO
import io.github.ndtung723.nihongoit.learningservice.dto.GetDueCardsResponseDto
import io.github.ndtung723.nihongoit.learningservice.dto.GetFlashcardResponseDto
import io.github.ndtung723.nihongoit.learningservice.dto.GetFlashcardsResponseDto
import io.github.ndtung723.nihongoit.learningservice.dto.PagedFlashcardsResponseDto
import io.github.ndtung723.nihongoit.learningservice.dto.ReviewFlashcardResponseDto
import io.github.ndtung723.nihongoit.learningservice.dto.UpdateFlashcardRequestDto
import io.github.ndtung723.nihongoit.learningservice.dto.UpdateFlashcardResponseDto
import io.github.ndtung723.nihongoit.learningservice.entity.FlashcardEntity
import io.github.ndtung723.nihongoit.learningservice.entity.ReviewLogEntity
import io.github.ndtung723.nihongoit.learningservice.entity.VocabularyEntity
import io.github.ndtung723.nihongoit.learningservice.repository.FlashcardRepository
import io.github.ndtung723.nihongoit.learningservice.repository.ReviewLogRepository
import io.github.ndtung723.nihongoit.learningservice.repository.VocabularyRepository
import io.github.ndtung723.nihongoit.learningservice.util.UserAuthUtil
import jakarta.persistence.EntityNotFoundException
import org.slf4j.LoggerFactory
import org.springframework.data.domain.PageRequest
import org.springframework.data.domain.Sort
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import java.nio.file.AccessDeniedException
import java.time.LocalDateTime
import java.time.temporal.ChronoUnit
import java.util.*

@Service
class FlashcardCrudService(
    private val flashcardRepository: FlashcardRepository,
    private val reviewLogRepository: ReviewLogRepository,
    private val vocabularyRepository: VocabularyRepository,
    private val fsrsService: FSRSService,
    private val userAuthUtil: UserAuthUtil,
    private val userProgressService: UserProgressService,
) {
    private val logger = LoggerFactory.getLogger(FlashcardCrudService::class.java)

    // Get due flashcards
    @Transactional(readOnly = true)
    fun getDueCards(): GetDueCardsResponseDto {
        val userId = userAuthUtil.getCurrentUserId()
        logger.debug("Getting due cards for user: $userId")
        val dueCards = flashcardRepository.findDueCards(requireNotNull(userId) { "User not authenticated" }, LocalDateTime.now())

        return GetDueCardsResponseDto(
            data = dueCards.map { toDTO(it) },
        )
    }

    // Get all flashcards (unpaged, kept for backward compatibility)
    @Transactional(readOnly = true)
    fun getAllFlashcards(): GetFlashcardsResponseDto {
        val userId = userAuthUtil.getCurrentUserId()
        logger.debug("Getting all flashcards for user: $userId")
        val allCards = flashcardRepository.findByUserId(requireNotNull(userId) { "User not authenticated" })

        return GetFlashcardsResponseDto(
            data = allCards.map { toDTO(it) },
        )
    }

    // Get flashcards with pagination
    @Transactional(readOnly = true)
    fun getAllFlashcardsPaged(
        page: Int,
        size: Int,
    ): PagedFlashcardsResponseDto {
        val userId = userAuthUtil.getCurrentUserId()
        logger.debug("Getting flashcards page=$page size=$size for user: $userId")
        val pageable = PageRequest.of(page, size, Sort.by("createdAt").descending())
        val flashcardPage =
            flashcardRepository.findByUserId(
                requireNotNull(userId) { "User not authenticated" },
                pageable,
            )

        return PagedFlashcardsResponseDto(
            data = flashcardPage.content.map { toDTO(it) },
            page = flashcardPage.number,
            size = flashcardPage.size,
            totalElements = flashcardPage.totalElements,
            totalPages = flashcardPage.totalPages,
        )
    }

    // Get a specific flashcard — returns ServiceResult to make error paths explicit
    @Transactional(readOnly = true)
    fun getFlashcardById(flashcardId: UUID): ServiceResult<GetFlashcardResponseDto> {
        val userId = userAuthUtil.getCurrentUserId()
        val flashcard =
            flashcardRepository.findById(flashcardId).orElse(null)
                ?: return ServiceResult.Failure("Flashcard not found with id: $flashcardId")

        if (flashcard.userId != userId) {
            return ServiceResult.Failure("User does not have access to this flashcard")
        }

        return ServiceResult.Success(GetFlashcardResponseDto(data = toDTO(flashcard)))
    }

    // Process flashcard review
    @Transactional
    fun processReview(
        flashcardId: UUID,
        rating: Int,
    ): ReviewFlashcardResponseDto {
        val userId = userAuthUtil.getCurrentUserId()
        logger.info("Processing review for flashcard: $flashcardId with rating: $rating")

        val flashcard =
            flashcardRepository
                .findById(flashcardId)
                .orElseThrow { EntityNotFoundException("Flashcard not found with id: $flashcardId") }

        if (flashcard.userId != userId) {
            throw AccessDeniedException("User does not have access to this flashcard")
        }

        if (rating < 1 || rating > 4) {
            throw BusinessException("Rating must be between 1 and 4")
        }

        // Calculate elapsed days since due date
        val now = LocalDateTime.now()
        val elapsedDays =
            if (flashcard.due.isBefore(now)) {
                ChronoUnit.DAYS
                    .between(flashcard.due, now)
                    .toDouble()
                    .coerceAtLeast(0.0)
            } else {
                0.0
            }

        // Get current state before processing
        val currentState = flashcard.state

        // Process review with FSRS
        val updatedFlashcard = fsrsService.processReview(flashcard, rating)

        // Create review log based on Go-FSRS structure
        val reviewLog =
            ReviewLogEntity(
                flashcard = updatedFlashcard,
                userId = requireNotNull(userId) { "User not authenticated" },
                rating = rating,
                scheduledDays = updatedFlashcard.scheduledDays,
                elapsedDays = elapsedDays,
                reviewTimestamp = now,
                state = currentState, // State before review
            )

        // Save review log
        reviewLogRepository.save(reviewLog)

        logger.info("Updating user_progress streak for user $userId after successful review")
        userProgressService.updateStreak(userId)

        return ReviewFlashcardResponseDto(
            data = toDTO(updatedFlashcard),
        )
    }

    // Create new flashcard
    @Transactional
    fun createFlashcard(request: CreateFlashcardRequestDto): CreateFlashcardResponseDto {
        val userId =
            userAuthUtil.getCurrentUserId()
                ?: throw BusinessException("User not authenticated")
        logger.info("Creating new flashcard for user: $userId")

        val vocabulary =
            request.vocabularyId?.let { vocabularyRepository.findById(it).orElse(null) }

        val flashcard =
            FlashcardEntity(
                userId = userId,
                vocabulary = vocabulary,
                frontText = request.frontText,
                backText = request.backText,
            )

        val savedFlashcard = fsrsService.initializeFlashcard(flashcard)

        return CreateFlashcardResponseDto(data = toDTO(savedFlashcard))
    }

    // Create flashcard from vocabulary //use this method
    @Transactional
    fun createFlashcardFromVocabulary(vocabId: UUID): CreateFlashcardResponseDto {
        val userId =
            userAuthUtil.getCurrentUserId()
                ?: throw BusinessException("User not authenticated")
        logger.info("Creating flashcard from vocabulary: $vocabId for user: $userId")

        val vocabulary =
            vocabularyRepository
                .findById(vocabId)
                .orElseThrow { EntityNotFoundException("Vocabulary item not found with id: $vocabId") }

        val existingFlashcard = flashcardRepository.findByUserIdAndVocabulary_VocabId(userId, vocabId)
        if (existingFlashcard.isNotEmpty()) {
            throw BusinessException("Flashcard for this vocabulary item already exists")
        }

        val frontText = buildFrontText(vocabulary)
        val backText = buildBackText(vocabulary)

        val flashcard =
            FlashcardEntity(
                userId = userId,
                vocabulary = vocabulary,
                frontText = frontText,
                backText = backText,
            )

        val savedFlashcard = fsrsService.initializeFlashcard(flashcard)

        return CreateFlashcardResponseDto(data = toDTO(savedFlashcard))
    }

    // Helper to build front text from vocabulary
    private fun buildFrontText(vocabulary: VocabularyEntity): String {
        val sb = StringBuilder()

        // Add kanji if available
        if (!vocabulary.term.isNullOrBlank()) {
            sb.append(vocabulary.term)
        }

        return sb.toString()
    }

    // Helper to build back text from vocabulary
    private fun buildBackText(vocabulary: VocabularyEntity): String {
        val sb = StringBuilder()

        // Add meaning
        sb.append(vocabulary.meaning)
        sb.append("\n\n")

        // Add reading if kanji was used on front
        if (!vocabulary.term.isNullOrBlank()) {
            sb.append("Reading: ")
            sb.append(vocabulary.term)
            sb.append("\n\n")
        }

        // Add example if available
        if (!vocabulary.example.isNullOrBlank()) {
            sb.append("Example: ")
            sb.append(vocabulary.example)
            if (!vocabulary.exampleMeaning.isNullOrBlank()) {
                sb.append("\n")
                sb.append(vocabulary.exampleMeaning)
            }
        }

        return sb.toString()
    }

    // Update flashcard
    @Transactional
    fun updateFlashcard(
        flashcardId: UUID,
        request: UpdateFlashcardRequestDto,
    ): UpdateFlashcardResponseDto {
        val userId = userAuthUtil.getCurrentUserId()
        logger.info("Updating flashcard: $flashcardId")

        val existingFlashcard =
            flashcardRepository
                .findById(flashcardId)
                .orElseThrow { EntityNotFoundException("Flashcard not found with id: $flashcardId") }

        if (existingFlashcard.userId != userId) {
            throw AccessDeniedException("User does not have access to this flashcard")
        }

        existingFlashcard.frontText = request.frontText
        existingFlashcard.backText = request.backText

        val savedFlashcard = flashcardRepository.save(existingFlashcard)

        return UpdateFlashcardResponseDto(
            data = toDTO(savedFlashcard),
        )
    }

    // Delete flashcard
    @Transactional
    fun deleteFlashcard(flashcardId: UUID): DeleteFlashcardResponseDto {
        val userId = userAuthUtil.getCurrentUserId()
        logger.info("Deleting flashcard: $flashcardId")

        val flashcard =
            flashcardRepository
                .findById(flashcardId)
                .orElseThrow { EntityNotFoundException("Flashcard not found with id: $flashcardId") }

        if (flashcard.userId != userId) {
            throw AccessDeniedException("User does not have access to this flashcard")
        }

        flashcardRepository.delete(flashcard)

        return DeleteFlashcardResponseDto()
    }

    // Get flashcards for a vocabulary item
    @Transactional(readOnly = true)
    fun getFlashcardsByVocabulary(vocabId: UUID): GetFlashcardsResponseDto {
        val userId = userAuthUtil.getCurrentUserId()
        logger.debug("Getting flashcards for vocabulary: $vocabId and user: $userId")

        val flashcards =
            flashcardRepository.findByUserIdAndVocabulary_VocabId(
                requireNotNull(userId) { "User not authenticated" },
                vocabId,
            )

        return GetFlashcardsResponseDto(
            data = flashcards.map { toDTO(it) },
        )
    }

    internal fun toDTO(flashcard: FlashcardEntity): FlashcardDTO = flashcard.toDto()

    /**
     * Get count of flashcards created after a specific time
     */
    fun getFlashcardsCreatedCount(afterDate: LocalDateTime): Int = flashcardRepository.countByCreatedAtAfter(afterDate).toInt()

    /**
     * Get count of flashcards studied (reviewed) after a specific time
     */
    fun getFlashcardsStudiedCount(afterDate: LocalDateTime): Int {
        // Count distinct flashcards that have been reviewed
        return reviewLogRepository.countDistinctFlashcardIdByReviewTimestampAfter(afterDate).toInt()
    }
}
