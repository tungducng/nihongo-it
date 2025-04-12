package com.example.nihongoit.service

import com.example.nihongoit.dto.ResponseDto
import com.example.nihongoit.dto.ResponseType
import com.example.nihongoit.dto.flashcard.*
import com.example.nihongoit.dto.review.ReviewFlashcardResponseDto
import com.example.nihongoit.entity.FlashcardEntity
import com.example.nihongoit.entity.ReviewLogEntity
import com.example.nihongoit.exception.BusinessException
import com.example.nihongoit.repository.FlashcardRepository
import com.example.nihongoit.repository.ReviewLogRepository
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
        val allCards = flashcardRepository.findByUserId(userId!!)
        
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
            
        if (flashcard.userId != userId) {
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
            
        if (flashcard.userId != userId) {
            throw AccessDeniedException("User does not have access to this flashcard")
        }
        
        if (rating < 1 || rating > 4) {
            throw BusinessException("Rating must be between 1 and 4")
        }
        
        // Create review log
        val reviewLog = ReviewLogEntity(
            flashcard = flashcard,
            userId = userId,
            rating = rating,
            state = flashcard.state,
            elapsedDays = ChronoUnit.DAYS.between(flashcard.due, LocalDateTime.now()).toDouble()
        )
        
        // Update FSRS parameters
        val result = fsrsService.updateCardParameters(flashcard, rating)
        
        // Update the card
        val now = LocalDateTime.now()
        flashcard.state = result.state
        flashcard.stability = result.stability
        flashcard.difficulty = result.difficulty
        flashcard.elapsedDays = 0.0
        flashcard.scheduledDays = result.interval
        flashcard.due = now.plusDays(result.interval.toLong())
        flashcard.reps += 1
        if (rating == 1) flashcard.lapses += 1
        flashcard.reviewLogs.add(reviewLog)
        
        // Save to database
        reviewLogRepository.save(reviewLog)
        val savedFlashcard = flashcardRepository.save(flashcard)
        
        val responseResult = ResponseDto(
            status = ResponseType.OK,
            message = "Flashcard reviewed successfully"
        )
        
        return ReviewFlashcardResponseDto(
            result = responseResult,
            data = toDTO(savedFlashcard)
        )
    }
    
    // Create new flashcard
    @Transactional
    fun createFlashcard(request: CreateFlashcardRequestDto): CreateFlashcardResponseDto {
        val userId = userAuthUtil.getCurrentUserId()
        logger.info("Creating new flashcard for user: $userId")
        
        val flashcard = FlashcardEntity(
            userId = userId!!,
            frontText = request.frontText,
            backText = request.backText,
            notes = request.notes,
            tags = request.tags
        )
        
        val savedFlashcard = flashcardRepository.save(flashcard)
        
        val result = ResponseDto(
            status = ResponseType.OK,
            message = "Flashcard created successfully"
        )
        
        return CreateFlashcardResponseDto(
            result = result,
            data = toDTO(savedFlashcard)
        )
    }
    
    // Update flashcard
    @Transactional
    fun updateFlashcard(flashcardId: UUID, request: UpdateFlashcardRequestDto): UpdateFlashcardResponseDto {
        val userId = userAuthUtil.getCurrentUserId()
        logger.info("Updating flashcard: $flashcardId")
        
        val existingFlashcard = flashcardRepository.findById(flashcardId)
            .orElseThrow { EntityNotFoundException("Flashcard not found with id: $flashcardId") }
            
        if (existingFlashcard.userId != userId) {
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
            
        if (flashcard.userId != userId) {
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
        
        val totalCards = flashcardRepository.countByUserId(userId!!)
        val dueCards = flashcardRepository.findDueCards(userId, LocalDateTime.now()).size
        
        // Statistics for the last 7 days
        val sevenDaysAgo = LocalDateTime.now().minusDays(7)
        val recentReviews = reviewLogRepository.findByUserIdAndTimestampAfter(userId, sevenDaysAgo)
        
        val dailyReviews = recentReviews.groupBy { it.timestamp.toLocalDate() }
            .mapValues { it.value.size }
        
        val statistics = mapOf(
            "totalCards" to totalCards,
            "dueCards" to dueCards,
            "reviewsLast7Days" to recentReviews.size,
            "dailyReviews" to dailyReviews
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
    
    // Convert entity to DTO
    fun toDTO(flashcard: FlashcardEntity): FlashcardDTO {
        return FlashcardDTO(
            id = flashcard.flashCardId,
            frontText = flashcard.frontText,
            backText = flashcard.backText,
            notes = flashcard.notes,
            tags = flashcard.tags,
            due = flashcard.due,
            reps = flashcard.reps,
            lapses = flashcard.lapses
        )
    }
}