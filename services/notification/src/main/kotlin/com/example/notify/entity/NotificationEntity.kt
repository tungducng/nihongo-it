package com.example.notify.entity

import com.example.common.entity.AbstractAuditEntity
import jakarta.persistence.*
import java.time.LocalDateTime
import java.util.*

@Entity
@Table(name = "notifications")
data class NotificationEntity(
    @Id
    @GeneratedValue(generator = "UUID")
    @Column(name = "notification_id", updatable = false, nullable = false)
    val notificationId: UUID? = null,
    // Cross-service reference — no FK constraint, no local users table.
    // The authoritative user record lives in user-service.
    @Column(name = "user_id", nullable = false)
    val userId: UUID,
    @Column(name = "title", nullable = false)
    val title: String,
    @Column(name = "message", columnDefinition = "text")
    val message: String,
    @Column(name = "type")
    @Enumerated(EnumType.STRING)
    val type: NotificationType,
    @Column(name = "is_read")
    val isRead: Boolean = false,
    @Column(name = "action_url")
    val actionUrl: String? = null,
    @Column(name = "notification_channel")
    @Enumerated(EnumType.STRING)
    val notificationChannel: NotificationChannel = NotificationChannel.EMAIL,
    @Column(name = "sent_at")
    val sentAt: LocalDateTime = LocalDateTime.now(),
    @Column(name = "read_at")
    val readAt: LocalDateTime? = null,
    @Column(name = "review_count")
    val reviewCount: Int? = null,
    @Column(name = "review_category")
    val reviewCategory: String? = null,
    @Column(name = "priority_level")
    val priorityLevel: Int = 0,
    @Column(name = "scheduled_for")
    val scheduledFor: LocalDateTime? = null,
    @Column(name = "external_id")
    val externalId: String? = null,
) : AbstractAuditEntity()

enum class NotificationType {
    STUDY_REMINDER, // General study reminder
    REVIEW_DUE, // FSRS-calculated flashcard review due
    SYSTEM_ANNOUNCEMENT, // System announcements
}

enum class NotificationChannel {
    APP,
    EMAIL,
    PUSH,
}
