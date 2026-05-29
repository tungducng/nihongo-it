package io.github.ndtung723.nihongoit.notify.controller

import io.github.ndtung723.nihongoit.notify.dto.NotificationDto
import io.github.ndtung723.nihongoit.notify.entity.NotificationType
import io.github.ndtung723.nihongoit.notify.service.NotificationService
import io.swagger.v3.oas.annotations.Operation
import io.swagger.v3.oas.annotations.tags.Tag
import jakarta.validation.constraints.NotBlank
import org.springframework.security.access.prepost.PreAuthorize
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController
import java.util.UUID

/**
 * Cross-service endpoint for other microservices to push notifications on
 * behalf of an end user (e.g. learning-service pushing a COMMENT_REPLY when
 * someone replies to your vocab comment). Gated by [InternalApiKeyFilter]
 * which checks `X-Internal-API-Key` — not exposed via the public gateway
 * routes.
 */
@RestController
@RequestMapping("/api/v1/notify/internal/notifications")
@Tag(name = "Internal notifications", description = "Service-to-service notification creation")
class InternalNotificationController(
    private val notificationService: NotificationService,
) {
    @PostMapping
    @PreAuthorize("hasRole('SERVICE')")
    @Operation(summary = "Create an in-app notification on behalf of a user")
    fun create(
        @RequestBody body: CreateInternalNotificationRequest,
    ): NotificationDto {
        val saved =
            notificationService.sendNotification(
                userId = body.userId,
                recipientEmail = body.recipientEmail.orEmpty(),
                channels = body.channels?.takeIf { it.isNotEmpty() } ?: setOf("app"),
                title = body.title,
                message = body.message,
                type = NotificationType.valueOf(body.type),
                actionUrl = body.actionUrl,
                priorityLevel = body.priorityLevel ?: 0,
            )
        return NotificationDto.fromEntity(saved)
    }
}

// Jackson + Kotlin can't reliably apply defaults to non-null primitives /
// non-null collections when the JSON key is omitted (see P9.3 in plan). Keep
// `channels` + `priorityLevel` nullable so callers can drop them entirely.
data class CreateInternalNotificationRequest(
    val userId: UUID,
    @field:NotBlank val title: String,
    @field:NotBlank val message: String,
    @field:NotBlank val type: String,
    val actionUrl: String? = null,
    val recipientEmail: String? = null,
    val channels: Set<String>? = null,
    val priorityLevel: Int? = null,
)
