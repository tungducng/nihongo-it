package com.example.nihongoit.controller

import com.example.nihongoit.entity.NotificationType
import com.example.nihongoit.repository.FlashcardRepository
import com.example.nihongoit.repository.UserRepository
import com.example.nihongoit.service.NotificationService
import com.example.nihongoit.service.ScheduledReminderService
import com.example.nihongoit.util.UserAuthUtil
import io.swagger.v3.oas.annotations.Operation
import io.swagger.v3.oas.annotations.tags.Tag
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController
import java.time.LocalDateTime

/**
 * Controller for testing various features
 * Note: This controller should be disabled in production
 */
@RestController
@RequestMapping("/api/v1/test")
@Tag(name = "Testing", description = "Endpoints for testing features (disable in production)")
class TestController @Autowired constructor(
    private val userAuthUtil: UserAuthUtil,
    private val userRepository: UserRepository,
    private val flashcardRepository: FlashcardRepository,
    private val notificationService: NotificationService,
    private val scheduledReminderService: ScheduledReminderService
) {

    /**
     * Test sending a flashcard reminder email to the current user
     */
    @GetMapping("/send-reminder-email")
    @Operation(summary = "Test sending a flashcard reminder email to the current user")
    fun testFlashcardReminderEmail(): ResponseEntity<Any> {
        // Get current user
        val userId = userAuthUtil.getCurrentUserId()
            ?: return ResponseEntity.badRequest().body("User not authenticated")
            
        val user = userRepository.findById(userId).orElse(null)
            ?: return ResponseEntity.badRequest().body("User not found")
            
        // Get due cards for this user
        val now = LocalDateTime.now()
        val dueCards = flashcardRepository.findDueCards(userId, now)
        
        if (dueCards.isEmpty()) {
            return ResponseEntity.ok(mapOf(
                "message" to "No due cards found for testing",
                "status" to "warning"
            ))
        }
        
        // Create action URL for the notification
        val actionUrl = "/flashcards/study"
        
        // Force send notification with email, regardless of user preferences
        val notification = notificationService.sendNotification(
            user = user,
            title = "TEST: Thẻ ghi nhớ cần ôn tập",
            message = "Bạn có ${dueCards.size} thẻ ghi nhớ đang chờ được ôn tập. [Email thử nghiệm]",
            type = NotificationType.REVIEW_DUE,
            actionUrl = actionUrl,
            reviewCount = dueCards.size,
            reviewCategory = "flashcard"
        )
        
        // Force send HTML email using the specialized flashcard reminder method
        val baseUrl = "http://localhost:5173"
        val fullActionUrl = "$baseUrl$actionUrl"
        notificationService.sendFlashcardReminderEmail(
            to = user.email,
            cardCount = dueCards.size,
            actionUrl = fullActionUrl
        )
        
        return ResponseEntity.ok(mapOf(
            "message" to "Test email sent successfully with new HTML template",
            "emailSentTo" to user.email,
            "cardCount" to dueCards.size,
            "notificationId" to notification.notificationId
        ))
    }
}