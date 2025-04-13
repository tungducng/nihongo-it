package com.example.nihongoit.service

import com.example.nihongoit.dto.ResponseDto
import com.example.nihongoit.dto.ResponseType
import com.example.nihongoit.dto.flashcard.*
import com.example.nihongoit.dto.review.ReviewFlashcardResponseDto
import com.example.nihongoit.entity.FlashcardEntity
import com.example.nihongoit.entity.ReviewLogEntity
import com.example.nihongoit.entity.UserEntity
import com.example.nihongoit.entity.VocabularyEntity
import com.example.nihongoit.exception.BusinessException
import com.example.nihongoit.repository.FlashcardRepository
import com.example.nihongoit.repository.ReviewLogRepository
import com.example.nihongoit.repository.UserRepository
import com.example.nihongoit.repository.VocabularyRepository
import com.example.nihongoit.util.UserAuthUtil
import jakarta.persistence.EntityNotFoundException
import org.slf4j.LoggerFactory
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import java.nio.file.AccessDeniedException
import java.time.LocalDateTime
import java.time.temporal.ChronoUnit
import java.util.*

@Service
class FlashcardService(
    private val flashcardRepository: FlashcardRepository,
    private val reviewLogRepository: ReviewLogRepository,
    private val userRepository: UserRepository,
    private val vocabularyRepository: VocabularyRepository,
    private val fsrsService: FSRSService,
    private val userAuthUtil: UserAuthUtil
) {
    private val logger = LoggerFactory.getLogger(FlashcardService::class.java)
    
    // Get due flashcards
    fun getDueCards(): GetDueCardsResponseDto {
        val userId = userAuthUtil.getCurrentUserId()
        logger.info("Getting due cards for user: $userId")
        val dueCards = flashcardRepository.findDueCards(userId!!, LocalDateTime.now())
        
        val result = ResponseDto(
            status = ResponseType.OK,
            message = "Due cards retrieved successfully"
        )
        
        return GetDueCardsResponseDto(
            result = result,
            data = dueCards.map { toDTO(it) }
        )
    }
    
    // Get all flashcards
    fun getAllFlashcards(): GetFlashcardsResponseDto {
        val userId = userAuthUtil.getCurrentUserId()
        logger.info("Getting all flashcards for user: $userId")
        val allCards = flashcardRepository.findByUser_UserId(userId!!)
        
        val result = ResponseDto(
            status = ResponseType.OK,
            message = "All flashcards retrieved successfully"
        )
        
        return GetFlashcardsResponseDto(
            result = result,
            data = allCards.map { toDTO(it) }
        )
    }
    
    // Get a specific flashcard
    fun getFlashcardById(flashcardId: UUID): GetFlashcardResponseDto {
        val userId = userAuthUtil.getCurrentUserId()
        val flashcard = flashcardRepository.findById(flashcardId)
            .orElseThrow { EntityNotFoundException("Flashcard not found with id: $flashcardId") }
            
        if (flashcard.user.userId != userId) {
            throw AccessDeniedException("User does not have access to this flashcard")
        }
        
        val result = ResponseDto(
            status = ResponseType.OK,
            message = "Flashcard retrieved successfully"
        )
        
        return GetFlashcardResponseDto(
            result = result,
            data = toDTO(flashcard)
        )
    }
    
    // Process flashcard review
    @Transactional
    fun processReview(flashcardId: UUID, rating: Int): ReviewFlashcardResponseDto {
        val userId = userAuthUtil.getCurrentUserId()
        logger.info("Processing review for flashcard: $flashcardId with rating: $rating")
        
        val flashcard = flashcardRepository.findById(flashcardId)
            .orElseThrow { EntityNotFoundException("Flashcard not found with id: $flashcardId") }
            
        if (flashcard.user.userId != userId) {
            throw AccessDeniedException("User does not have access to this flashcard")
        }
        
        if (rating < 1 || rating > 4) {
            throw BusinessException("Rating must be between 1 and 4")
        }
        
        // Calculate elapsed days since due date
        val now = LocalDateTime.now()
        val elapsedDays = if (flashcard.due.isBefore(now)) {
            ChronoUnit.DAYS.between(flashcard.due, now).toDouble().coerceAtLeast(0.0)
        } else {
            0.0
        }
        
        // Get current state before processing
        val currentState = flashcard.state
        
        // Process review with FSRS
        val updatedFlashcard = fsrsService.processReview(flashcard, rating)
        
        // Create review log based on Go-FSRS structure
        val reviewLog = ReviewLogEntity(
            flashcard = updatedFlashcard,
            userId = userId!!,
            rating = rating,
            scheduledDays = updatedFlashcard.scheduledDays,
            elapsedDays = elapsedDays,
            reviewTimestamp = now,
            state = currentState // State before review
        )
        
        // Save review log
        reviewLogRepository.save(reviewLog)
        
        val result = ResponseDto(
            status = ResponseType.OK,
            message = "Flashcard reviewed successfully"
        )
        
        return ReviewFlashcardResponseDto(
            result = result,
            data = toDTO(updatedFlashcard)
        )
    }
    
    // Create new flashcard
    @Transactional
    fun createFlashcard(request: CreateFlashcardRequestDto): CreateFlashcardResponseDto {
        val userId = userAuthUtil.getCurrentUserId()
        logger.info("Creating new flashcard for user: $userId")
        
        val user = userRepository.findById(userId!!)
            .orElseThrow { EntityNotFoundException("User not found with id: $userId") }
        
        // Check if there's a vocabulary reference
        val vocabulary = if (request.vocabularyId != null) {
            vocabularyRepository.findById(request.vocabularyId)
                .orElse(null)
        } else {
            null
        }
        
        val flashcard = FlashcardEntity(
            user = user,
            vocabulary = vocabulary,
            frontText = request.frontText,
            backText = request.backText,
            notes = request.notes,
            tags = request.tags
        )
        
        // Initialize with FSRS default values
        val savedFlashcard = fsrsService.initializeFlashcard(flashcard)
        
        val result = ResponseDto(
            status = ResponseType.OK,
            message = "Flashcard created successfully"
        )
        
        return CreateFlashcardResponseDto(
            result = result,
            data = toDTO(savedFlashcard)
        )
    }
    
    // Create flashcard from vocabulary
    @Transactional
    fun createFlashcardFromVocabulary(vocabId: UUID): CreateFlashcardResponseDto {
        val userId = userAuthUtil.getCurrentUserId()
        logger.info("Creating flashcard from vocabulary: $vocabId for user: $userId")
        
        val user = userRepository.findById(userId!!)
            .orElseThrow { EntityNotFoundException("User not found with id: $userId") }
        
        val vocabulary = vocabularyRepository.findById(vocabId)
            .orElseThrow { EntityNotFoundException("Vocabulary item not found with id: $vocabId") }
        
        // Check if flashcard already exists for this vocabulary and user
        val existingFlashcard = flashcardRepository.findByUser_UserIdAndVocabulary_VocabId(userId, vocabId)
        if (existingFlashcard.isNotEmpty()) {
            throw BusinessException("Flashcard for this vocabulary item already exists")
        }
        
        // Create front and back text based on vocabulary
        val frontText = buildFrontText(vocabulary)
        val backText = buildBackText(vocabulary)
        
        val flashcard = FlashcardEntity(
            user = user,
            vocabulary = vocabulary,
            frontText = frontText,
            backText = backText,
            notes = null,
            tags = vocabulary.category
        )
        
        // Initialize with FSRS default values
        val savedFlashcard = fsrsService.initializeFlashcard(flashcard)
        
        val result = ResponseDto(
            status = ResponseType.OK,
            message = "Flashcard created from vocabulary successfully"
        )
        
        return CreateFlashcardResponseDto(
            result = result,
            data = toDTO(savedFlashcard)
        )
    }
    
    // Helper to build front text from vocabulary
    private fun buildFrontText(vocabulary: VocabularyEntity): String {
        val sb = StringBuilder()
        
        // Add kanji if available
        if (!vocabulary.kanji.isNullOrBlank()) {
            sb.append(vocabulary.kanji)
        } else if (!vocabulary.katakana.isNullOrBlank()) {
            sb.append(vocabulary.katakana)
        } else {
            sb.append(vocabulary.hiragana)
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
        if (!vocabulary.kanji.isNullOrBlank()) {
            sb.append("Reading: ")
            sb.append(vocabulary.hiragana)
            sb.append("\n\n")
        }
        
        // Add example if available
        if (!vocabulary.exampleSentence.isNullOrBlank()) {
            sb.append("Example: ")
            sb.append(vocabulary.exampleSentence)
            if (!vocabulary.exampleSentenceTranslation.isNullOrBlank()) {
                sb.append("\n")
                sb.append(vocabulary.exampleSentenceTranslation)
            }
        }
        
        return sb.toString()
    }
    
    // Update flashcard
    @Transactional
    fun updateFlashcard(flashcardId: UUID, request: UpdateFlashcardRequestDto): UpdateFlashcardResponseDto {
        val userId = userAuthUtil.getCurrentUserId()
        logger.info("Updating flashcard: $flashcardId")
        
        val existingFlashcard = flashcardRepository.findById(flashcardId)
            .orElseThrow { EntityNotFoundException("Flashcard not found with id: $flashcardId") }
            
        if (existingFlashcard.user.userId != userId) {
            throw AccessDeniedException("User does not have access to this flashcard")
        }
        
        existingFlashcard.frontText = request.frontText
        existingFlashcard.backText = request.backText
        existingFlashcard.notes = request.notes
        existingFlashcard.tags = request.tags
        
        val savedFlashcard = flashcardRepository.save(existingFlashcard)
        
        val result = ResponseDto(
            status = ResponseType.OK,
            message = "Flashcard updated successfully"
        )
        
        return UpdateFlashcardResponseDto(
            result = result,
            data = toDTO(savedFlashcard)
        )
    }
    
    // Delete flashcard
    @Transactional
    fun deleteFlashcard(flashcardId: UUID): DeleteFlashcardResponseDto {
        val userId = userAuthUtil.getCurrentUserId()
        logger.info("Deleting flashcard: $flashcardId")
        
        val flashcard = flashcardRepository.findById(flashcardId)
            .orElseThrow { EntityNotFoundException("Flashcard not found with id: $flashcardId") }
            
        if (flashcard.user.userId != userId) {
            throw AccessDeniedException("User does not have access to this flashcard")
        }
        
        flashcardRepository.delete(flashcard)
        
        val result = ResponseDto(
            status = ResponseType.OK,
            message = "Flashcard deleted successfully"
        )
        
        return DeleteFlashcardResponseDto(
            result = result
        )
    }
    
    // Get study statistics
    fun getStudyStatistics(): GetStatisticsResponseDto {
        val userId = userAuthUtil.getCurrentUserId()
        logger.info("Getting study statistics for user: $userId")
        
        val totalCards = flashcardRepository.countByUser_UserId(userId!!)
        val dueCards = flashcardRepository.findDueCards(userId, LocalDateTime.now()).size
        
        // Statistics for the last 7 days
        val sevenDaysAgo = LocalDateTime.now().minusDays(7)
        val recentReviews = reviewLogRepository.findByUserIdAndReviewTimestampAfter(userId, sevenDaysAgo)
        
        val dailyReviews = recentReviews.groupBy { it.reviewTimestamp.toLocalDate() }
            .mapValues { it.value.size }
            
        // Calculate retention rate
        val correctReviews = recentReviews.count { it.rating >= 3 }
        val retentionRate = if (recentReviews.isNotEmpty()) {
            (correctReviews.toDouble() / recentReviews.size) * 100.0
        } else {
            0.0
        }
        
        val statistics = mapOf(
            "totalCards" to totalCards,
            "dueCards" to dueCards,
            "reviewsLast7Days" to recentReviews.size,
            "dailyReviews" to dailyReviews,
            "retentionRate" to retentionRate,
            "averageRating" to if (recentReviews.isNotEmpty()) recentReviews.map { it.rating }.average() else 0.0
        )
        
        val result = ResponseDto(
            status = ResponseType.OK,
            message = "Study statistics retrieved successfully"
        )
        
        return GetStatisticsResponseDto(
            result = result,
            data = statistics
        )
    }
    
    // Simulate review outcomes for a flashcard
    fun simulateReview(flashcardId: UUID): Map<String, Any> {
        val userId = userAuthUtil.getCurrentUserId()
        val flashcard = flashcardRepository.findById(flashcardId)
            .orElseThrow { EntityNotFoundException("Flashcard not found with id: $flashcardId") }
            
        if (flashcard.user.userId != userId) {
            throw AccessDeniedException("User does not have access to this flashcard")
        }
        
        val simulationResults = fsrsService.simulateReview(flashcard)
        
        return simulationResults.mapKeys { it.key.name.lowercase() }
            .mapValues { 
                mapOf(
                    "due" to it.value.due,
                    "interval" to it.value.scheduledDays,
                    "state" to FSRSService.State.values()[it.value.state].name.lowercase(),
                    "stability" to it.value.stability.toFloat(),
                    "difficulty" to it.value.difficulty.toFloat()
                )
            }
    }
    
    // Get flashcards for a vocabulary item
    fun getFlashcardsByVocabulary(vocabId: UUID): GetFlashcardsResponseDto {
        val userId = userAuthUtil.getCurrentUserId()
        logger.info("Getting flashcards for vocabulary: $vocabId and user: $userId")
        
        val flashcards = flashcardRepository.findByVocabulary_VocabId(vocabId)
            .filter { it.user.userId == userId }
        
        val result = ResponseDto(
            status = ResponseType.OK,
            message = "Vocabulary flashcards retrieved successfully"
        )
        
        return GetFlashcardsResponseDto(
            result = result,
            data = flashcards.map { toDTO(it) }
        )
    }
    
    // Convert entity to DTO
    fun toDTO(flashcard: FlashcardEntity): FlashcardDTO {
        return FlashcardDTO(
            id = flashcard.flashCardId,
            frontText = flashcard.frontText,
            backText = flashcard.backText,
            notes = flashcard.notes,
            tags = flashcard.tags,
            vocabularyId = flashcard.vocabulary?.vocabId,
            due = flashcard.due,
            reps = flashcard.reps,
            lapses = flashcard.lapses,
            state = FSRSService.State.values().find { it.value == flashcard.state }?.name?.lowercase() ?: "new",
            difficulty = flashcard.difficulty,
            stability = flashcard.stability,
            interval = flashcard.scheduledDays
        )
    }
}