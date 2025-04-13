//package com.example.nihongoit.service
//
//import com.example.nihongoit.entity.NotificationEntity
//import com.example.nihongoit.entity.NotificationType
//import com.example.nihongoit.entity.NotificationChannel
//import com.example.nihongoit.entity.UserEntity
//import com.example.nihongoit.repository.NotificationRepository
//import org.slf4j.LoggerFactory
//import org.springframework.beans.factory.annotation.Autowired
//import org.springframework.beans.factory.annotation.Value
//import org.springframework.mail.SimpleMailMessage
//import org.springframework.mail.javamail.JavaMailSender
//import org.springframework.scheduling.annotation.Async
//import org.springframework.scheduling.annotation.Scheduled
//import org.springframework.stereotype.Service
//import org.springframework.transaction.annotation.Transactional
//import java.time.LocalDateTime
//import java.util.UUID
//
///**
// * Service for managing notifications for the Japanese IT vocabulary learning application
// * Currently implements email notifications with in-app notifications stored in database
// */
//@Service
//class NotificationService @Autowired constructor(
//    private val notificationRepository: NotificationRepository,
//    private val emailSender: JavaMailSender
//) {
//    private val logger = LoggerFactory.getLogger(NotificationService::class.java)
//
//    @Value("\${spring.mail.username}")
//    private lateinit var senderEmail: String
//
//    @Value("\${app.frontend-url}")
//    private lateinit var frontendUrl: String
//
//    /**
//     * Send a notification to a user through their preferred channels
//     */
//    @Transactional
//    fun sendNotification(
//        user: UserEntity,
//        title: String,
//        message: String,
//        type: NotificationType,
//        actionUrl: String? = null,
//        reviewCount: Int? = null,
//        reviewCategory: String? = null,
//        priorityLevel: Int = 0
//    ): NotificationEntity {
//        logger.debug("Sending notification to user ${user.userId} - Type: $type, Title: $title")
//
//        // Create notification record
//        val notification = NotificationEntity(
//            user = user,
//            title = title,
//            message = message,
//            type = type,
//            actionUrl = actionUrl,
//            sentAt = LocalDateTime.now(),
//            reviewCount = reviewCount,
//            reviewCategory = reviewCategory,
//            priorityLevel = priorityLevel,
//            notificationChannel = NotificationChannel.APP
//        )
//
//        // Save to database first (in-app notification)
//        val savedNotification = notificationRepository.save(notification)
//
//        // Get user preferences
//        val channels = user.notificationPreferences.split(",").map { it.trim() }
//
//        // Send email notification if enabled
//        if (channels.contains("email")) {
//            sendEmailNotification(user.email, title, message, actionUrl)
//        }
//
//        return savedNotification
//    }
//
//    /**
//     * Send a reminder to study based on user's schedule
//     */
//    fun sendStudyReminder(user: UserEntity) {
//        logger.debug("Sending study reminder to user ${user.userId}")
//
//        val title = "Time to Study!"
//        val message = "Don't forget to continue your Japanese IT vocabulary learning today to maintain your ${user.streakCount}-day streak!"
//
//        sendNotification(
//            user = user,
//            title = title,
//            message = message,
//            type = NotificationType.STUDY_REMINDER,
//            actionUrl = "$frontendUrl/dashboard"
//        )
//    }
//
//    /**
//     * Send notification for FSRS flashcard reviews that are due
//     */
//    fun sendFlashcardReviewDueNotification(
//        user: UserEntity,
//        reviewCount: Int,
//        priorityLevel: Int = 0
//    ) {
//        logger.debug("Sending flashcard review notification to user ${user.userId} - Count: $reviewCount")
//
//        val title = "Time to Review!"
//        val message = "You have $reviewCount IT flashcards due for review. Keep your knowledge fresh!"
//
//        sendNotification(
//            user = user,
//            title = title,
//            message = message,
//            type = NotificationType.REVIEW_DUE,
//            actionUrl = "$frontendUrl/flashcards/review",
//            reviewCount = reviewCount,
//            reviewCategory = "flashcard",
//            priorityLevel = priorityLevel
//        )
//    }
//
//    /**
//     * Send notification about a leech flashcard
//     */
//    fun sendLeechNotification(user: UserEntity, itemType: String, itemCount: Int = 1) {
//        logger.debug("Sending leech notification to user ${user.userId} - Type: $itemType, Count: $itemCount")
//
//        val title = "Difficult Flashcards Detected"
//        val message = if (itemCount == 1) {
//            "We've identified an IT flashcard that you're having trouble with. Consider using additional study techniques."
//        } else {
//            "We've identified $itemCount IT flashcards that you're having trouble with. Consider using additional study techniques."
//        }
//
//        sendNotification(
//            user = user,
//            title = title,
//            message = message,
//            type = NotificationType.LEECH_ALERT,
//            actionUrl = "$frontendUrl/flashcards/leech",
//            priorityLevel = 4  // Higher priority for leeches
//        )
//    }
//
//    /**
//     * Send quiz reminder
//     */
//    fun sendQuizReminder(user: UserEntity, quizTitle: String) {
//        logger.debug("Sending quiz reminder to user ${user.userId} - Quiz: $quizTitle")
//
//        val title = "Quiz Available"
//        val message = "Test your IT Japanese knowledge with \"$quizTitle\""
//
//        sendNotification(
//            user = user,
//            title = title,
//            message = message,
//            type = NotificationType.QUIZ_REMINDER,
//            actionUrl = "$frontendUrl/quizzes"
//        )
//    }
//
//    /**
//     * Send system announcement to all users
//     * Note: This implementation sends separate notifications to each user
//     * rather than using a broadcast mechanism.
//     */
//    @Transactional
//    fun sendSystemAnnouncement(users: List<UserEntity>, title: String, message: String, actionUrl: String? = null) {
//        logger.debug("Sending system announcement to ${users.size} users - Title: $title")
//
//        users.forEach { user ->
//            sendNotification(
//                user = user,
//                title = title,
//                message = message,
//                type = NotificationType.SYSTEM_ANNOUNCEMENT,
//                actionUrl = actionUrl
//            )
//        }
//    }
//
//    /**
//     * Mark notification as read
//     */
//    @Transactional
//    fun markAsRead(notificationId: UUID) {
//        val notification = notificationRepository.findById(notificationId).orElseThrow {
//            IllegalArgumentException("Notification not found")
//        }
//
//        val updatedNotification = notification.copy(
//            isRead = true,
//            readAt = LocalDateTime.now()
//        )
//
//        notificationRepository.save(updatedNotification)
//    }
//
//    /**
//     * Mark all notifications as read for a user
//     */
//    @Transactional
//    fun markAllAsRead(user: UserEntity) {
//        val unreadNotifications = notificationRepository.findByUserAndIsReadFalseOrderBySentAtDesc(user)
//        val now = LocalDateTime.now()
//
//        unreadNotifications.forEach { notification ->
//            val updatedNotification = notification.copy(
//                isRead = true,
//                readAt = now
//            )
//            notificationRepository.save(updatedNotification)
//        }
//    }
//
//    /**
//     * Get unread notifications for a user
//     */
//    fun getUnreadNotifications(user: UserEntity): List<NotificationEntity> {
//        return notificationRepository.findByUserAndIsReadFalseOrderBySentAtDesc(user)
//    }
//
//    /**
//     * Get all notifications for a user (with pagination)
//     */
//    fun getAllNotifications(user: UserEntity, page: Int, size: Int): List<NotificationEntity> {
//        // This would use Spring Data pagination in a real implementation
//        return notificationRepository.findByUserOrderBySentAtDesc(user)
//    }
//
//    /**
//     * Clean up old notifications (scheduled task)
//     */
//    @Scheduled(cron = "0 0 1 * * ?") // Run at 1:00 AM every day
//    @Transactional
//    fun cleanupOldNotifications() {
//        logger.info("Running scheduled task: cleanupOldNotifications")
//
//        val cutoffDate = LocalDateTime.now().minusMonths(3) // Keep 3 months of history
//
//        // Delete old notifications
//        notificationRepository.deleteByCreatedAtBefore(cutoffDate)
//    }
//
//    /**
//     * Send email notification
//     */
//    @Async
//    fun sendEmailNotification(to: String, subject: String, content: String, actionUrl: String? = null) {
//        try {
//            logger.debug("Sending email to $to - Subject: $subject")
//
//            val message = SimpleMailMessage()
//            message.setFrom(senderEmail)
//            message.setTo(to)
//            message.setSubject(subject)
//
//            // Build email content with action URL if provided
//            val emailContent = if (actionUrl != null) {
//                "$content\n\nClick here to view: $actionUrl"
//            } else {
//                content
//            }
//
//            message.setText(emailContent)
//            emailSender.send(message)
//
//            logger.debug("Email sent successfully to $to")
//        } catch (e: Exception) {
//            // In development, just log the error but don't fail
//            logger.error("Failed to send email to $to: ${e.message}")
//            logger.debug("Email content would have been: $content")
//        }
//    }
//}