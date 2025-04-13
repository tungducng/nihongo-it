package com.example.nihongoit.entity

import jakarta.persistence.*
import java.time.LocalDateTime
import java.util.*

@Entity
@Table(name = "notifications")
data class NotificationEntity(
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @Column(name = "notification_id")
    val notificationId: UUID = UUID.randomUUID(),

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id")
    val user: UserEntity,

    @Column(name = "title", nullable = false)
    val title: String,

    @Column(name = "message", columnDefinition = "text")
    val message: String?,

    @Enumerated(EnumType.STRING)
    @Column(name = "type", nullable = false)
    val type: NotificationType,

    @Column(name = "is_read")
    val isRead: Boolean = false,

    @Column(name = "scheduled_for")
    val scheduledFor: LocalDateTime?,

    @Column(name = "created_at")
    val createdAt: LocalDateTime = LocalDateTime.now(),
)

enum class NotificationType {
    STUDY_REMINDER,
    ACHIEVEMENT,
    QUIZ_RESULT,
}
