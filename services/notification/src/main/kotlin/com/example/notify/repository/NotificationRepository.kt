package com.example.notify.repository

import com.example.notify.entity.NotificationEntity
import com.example.notify.entity.NotificationType
import org.springframework.data.domain.Page
import org.springframework.data.domain.Pageable
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.jpa.repository.Modifying
import org.springframework.data.jpa.repository.Query
import org.springframework.data.repository.query.Param
import org.springframework.stereotype.Repository
import java.util.UUID

@Repository
interface NotificationRepository : JpaRepository<NotificationEntity, UUID> {
    fun findFirstByUserIdAndTypeOrderBySentAtDesc(
        userId: UUID,
        type: NotificationType,
    ): NotificationEntity?

    fun findByUserIdOrderBySentAtDesc(
        userId: UUID,
        pageable: Pageable,
    ): Page<NotificationEntity>

    fun countByUserIdAndIsReadFalse(userId: UUID): Long

    fun findByNotificationIdAndUserId(
        notificationId: UUID,
        userId: UUID,
    ): NotificationEntity?

    @Modifying
    @Query("UPDATE NotificationEntity n SET n.isRead = true WHERE n.userId = :userId AND n.isRead = false")
    fun markAllReadByUserId(
        @Param("userId") userId: UUID,
    ): Int

    @Modifying
    @Query("DELETE FROM NotificationEntity n WHERE n.notificationId = :id AND n.userId = :userId")
    fun deleteByIdAndUserId(
        @Param("id") id: UUID,
        @Param("userId") userId: UUID,
    ): Int
}
