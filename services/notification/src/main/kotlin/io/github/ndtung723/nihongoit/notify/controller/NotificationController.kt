package io.github.ndtung723.nihongoit.notify.controller

import io.github.ndtung723.nihongoit.notify.dto.NotificationDto
import io.github.ndtung723.nihongoit.notify.dto.PagedNotificationResponse
import io.github.ndtung723.nihongoit.notify.service.NotificationService
import io.swagger.v3.oas.annotations.Operation
import io.swagger.v3.oas.annotations.tags.Tag
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.security.core.context.SecurityContextHolder
import org.springframework.web.bind.annotation.*
import java.util.UUID

@RestController
@RequestMapping("/api/v1/notify/notifications")
@Tag(name = "Notifications", description = "User notification management")
class NotificationController(
    private val notificationService: NotificationService,
) {
    private fun currentUserId(): UUID = UUID.fromString(SecurityContextHolder.getContext().authentication?.principal as String)

    @GetMapping
    @Operation(summary = "Get paginated notifications for current user")
    fun getNotifications(
        @RequestParam(defaultValue = "0") page: Int,
        @RequestParam(defaultValue = "20") size: Int,
    ): PagedNotificationResponse {
        val result = notificationService.listForUser(currentUserId(), page, size)
        return PagedNotificationResponse(
            content = result.content.map { NotificationDto.fromEntity(it) },
            page = result.number,
            size = result.size,
            totalElements = result.totalElements,
            totalPages = result.totalPages,
            lastPage = result.isLast,
        )
    }

    @GetMapping("/unread-count")
    @Operation(summary = "Get unread notification count")
    fun getUnreadCount(): Map<String, Long> = mapOf("count" to notificationService.countUnreadForUser(currentUserId()))

    @PutMapping("/{id}/read")
    @Operation(summary = "Mark a notification as read")
    fun markAsRead(
        @PathVariable id: UUID,
    ): ResponseEntity<Map<String, String>> =
        if (notificationService.markAsReadForUser(currentUserId(), id)) {
            ResponseEntity.ok(mapOf("message" to "Notification marked as read"))
        } else {
            ResponseEntity.status(HttpStatus.NOT_FOUND).build()
        }

    @PutMapping("/read-all")
    @Operation(summary = "Mark all notifications as read")
    fun markAllAsRead(): Map<String, Int> = mapOf("updated" to notificationService.markAllAsReadForUser(currentUserId()))

    @DeleteMapping("/{id}")
    @Operation(summary = "Delete a notification")
    fun deleteNotification(
        @PathVariable id: UUID,
    ): ResponseEntity<Map<String, String>> =
        if (notificationService.deleteForUser(currentUserId(), id)) {
            ResponseEntity.ok(mapOf("message" to "Notification deleted"))
        } else {
            ResponseEntity.status(HttpStatus.NOT_FOUND).build()
        }
}
