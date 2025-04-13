//package com.example.nihongoit.service
//
//import com.example.nihongoit.entity.NotificationEntity
//import com.example.nihongoit.entity.NotificationChannel
//import com.example.nihongoit.entity.NotificationType
//import com.example.nihongoit.entity.UserEntity
//import com.example.nihongoit.repository.NotificationRepository
//import org.slf4j.LoggerFactory
//import org.springframework.beans.factory.annotation.Autowired
//import org.springframework.beans.factory.annotation.Value
//import org.springframework.context.annotation.Primary
//import org.springframework.context.annotation.Profile
//import org.springframework.stereotype.Service
//import org.springframework.transaction.annotation.Transactional
//import java.time.LocalDateTime
//import java.util.UUID
//
///**
// * Default implementation of the Notification Service
// * Used for testing and development environments without requiring email sending
// */
//@Service
//@Primary
//@Profile("test")
//class DefaultNotificationService @Autowired constructor(
//    private val notificationRepository: NotificationRepository
//) {
//    private val logger = LoggerFactory.getLogger(DefaultNotificationService::class.java)
//
//    @Value("\${app.frontend-url:http://localhost:3000}")
//    private lateinit var frontendUrl: String
//
//    /**
//     * Send a notification to a user (only creates in-app notification)
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
//        logger.debug("Creating in-app notification for user ${user.userId} - Type: $type, Title: $title")
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
//        // Save to database as in-app notification
//        return notificationRepository.save(notification)
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
//     * Get unread notifications for a user
//     */
//    fun getUnreadNotifications(user: UserEntity): List<NotificationEntity> {
//        return notificationRepository.findByUserAndIsReadFalseOrderBySentAtDesc(user)
//    }
//}