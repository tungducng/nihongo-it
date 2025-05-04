package com.example.nihongoit.service

import com.example.nihongoit.entity.NotificationEntity
import com.example.nihongoit.entity.NotificationType
import com.example.nihongoit.entity.NotificationChannel
import com.example.nihongoit.entity.UserEntity
import com.example.nihongoit.repository.NotificationRepository
import org.slf4j.LoggerFactory
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.beans.factory.annotation.Value
import org.springframework.mail.SimpleMailMessage
import org.springframework.mail.javamail.JavaMailSender
import org.springframework.scheduling.annotation.Async
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import java.time.LocalDateTime

/**
 * Service for managing notifications for the Japanese IT vocabulary learning application
 * Currently implements email notifications with in-app notifications stored in database
 */
@Service
class NotificationService @Autowired constructor(
    private val notificationRepository: NotificationRepository,
    private val emailSender: JavaMailSender
) {
    private val logger = LoggerFactory.getLogger(NotificationService::class.java)

    @Value("\${spring.mail.username:no.reply.nihongo.it@gmail.com}")
    private lateinit var senderEmail: String

    @Value("\${app.frontend-url:localhost:5173}")
    private lateinit var frontendUrl: String

    /**
     * Send a notification to a user through their preferred channels
     */
    @Transactional
    fun sendNotification(
        user: UserEntity,
        title: String,
        message: String,
        type: NotificationType,
        actionUrl: String? = null,
        reviewCount: Int? = null,
        reviewCategory: String? = null,
        priorityLevel: Int = 0
    ): NotificationEntity {
        logger.debug("Sending notification to user ${user.userId} - Type: $type, Title: $title")

        // Create notification record
        val notification = NotificationEntity(
            user = user,
            title = title,
            message = message,
            type = type,
            actionUrl = actionUrl,
            sentAt = LocalDateTime.now(),
            reviewCount = reviewCount,
            reviewCategory = reviewCategory,
            priorityLevel = priorityLevel,
            notificationChannel = NotificationChannel.APP
        )

        // Save to database first (in-app notification)
        val savedNotification = notificationRepository.save(notification)

        // Get user preferences
        val channels = user.notificationPreferences.split(",").map { it.trim() }

        // Send email notification if enabled
        if (channels.contains("email")) {
            sendEmailNotification(user.email, title, message, actionUrl)
        }

        return savedNotification
    }

    /**
     * Sends an email notification to the specified email address
     * 
     * @param to The recipient email address
     * @param subject The email subject
     * @param content The email content
     * @param actionUrl Optional URL for action buttons
     */
    @Async
    fun sendEmailNotification(to: String, subject: String, content: String, actionUrl: String? = null) {
        try {
            logger.debug("Sending email to $to - Subject: $subject")

            val message = SimpleMailMessage()
            message.setFrom(senderEmail)
            message.setTo(to)
            message.setSubject(subject)

            // Build email content with action URL if provided
            val emailContent = if (actionUrl != null) {
                "$content\n\nClick here to view: $actionUrl"
            } else {
                content
            }

            message.setText(emailContent)
            emailSender.send(message)

            logger.debug("Email sent successfully to $to")
        } catch (e: Exception) {
            // In development, just log the error but don't fail
            logger.error("Failed to send email to $to: ${e.message}")
            logger.debug("Email content would have been: $content")
        }
    }

    /**
     * Sends a password change email to the user (simplified version)
     * 
     * @param email The user's email address
     * @param resetToken The password reset token
     */
    @Async
    fun sendPasswordResetEmail(email: String, resetToken: String) {
        val frontendUrl = "http://localhost:5173" // Change this to match your frontend URL
        val resetUrl = "$frontendUrl/account/reset-password?token=$resetToken"
        
        sendPasswordResetEmail(email, resetToken, resetUrl)
    }

    /**
     * Sends a password change email to the user
     * 
     * @param email The user's email address
     * @param resetToken The password reset token
     * @param resetUrl The password change URL with token
     */
    @Async
    fun sendPasswordResetEmail(email: String, resetToken: String, resetUrl: String) {
        val subject = "Password Change Request - Nihongo IT"
        val content = """
            Hello,
            
            You have requested to change your password for your Nihongo IT account.
            
            Please use the following link to change your password:
            $resetUrl
            
            This link will expire in 30 minutes.
            
            If you did not request a password change, please ignore this email.
            
            Best regards,
            The Nihongo IT Team
        """.trimIndent()
        
        try {
            logger.debug("Sending password change email to $email")

            val message = SimpleMailMessage()
            message.setFrom(senderEmail)
            message.setTo(email)
            message.setSubject(subject)
            message.setText(content)
            
            emailSender.send(message)
            
            logger.debug("Password change email sent successfully to $email")
        } catch (e: Exception) {
            logger.error("Failed to send password change email to $email: ${e.message}")
            logger.debug("Email content would have been: $content")
        }
    }

    /**
     * Get the latest notification of a specific type for a user
     * 
     * @param user The user entity
     * @param type The notification type to search for
     * @return The latest notification of the specified type, or null if none found
     */
    fun getLastNotificationByType(user: UserEntity, type: NotificationType): NotificationEntity? {
        return notificationRepository.findFirstByUserAndTypeOrderBySentAtDesc(user, type)
    }
}