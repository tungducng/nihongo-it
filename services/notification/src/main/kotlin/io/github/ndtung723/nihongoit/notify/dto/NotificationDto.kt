package io.github.ndtung723.nihongoit.notify.dto

import com.fasterxml.jackson.annotation.JsonInclude
import com.fasterxml.jackson.annotation.JsonProperty
import io.github.ndtung723.nihongoit.notify.entity.NotificationEntity
import java.time.LocalDateTime
import java.util.UUID

@JsonInclude(JsonInclude.Include.NON_NULL)
data class NotificationDto(
    @JsonProperty("id")
    val id: UUID,
    @JsonProperty("title")
    val title: String,
    @JsonProperty("message")
    val message: String,
    @JsonProperty("type")
    val type: String,
    @JsonProperty("isRead")
    val isRead: Boolean,
    @JsonProperty("actionUrl")
    val actionUrl: String?,
    @JsonProperty("sentAt")
    val sentAt: LocalDateTime,
    @JsonProperty("readAt")
    val readAt: LocalDateTime?,
    @JsonProperty("reviewCount")
    val reviewCount: Int?,
    @JsonProperty("priorityLevel")
    val priorityLevel: Int,
) {
    companion object {
        fun fromEntity(entity: NotificationEntity): NotificationDto =
            NotificationDto(
                id = requireNotNull(entity.notificationId) { "Notification ID is null" },
                title = entity.title,
                message = entity.message,
                type = entity.type.name,
                isRead = entity.isRead,
                actionUrl = entity.actionUrl,
                sentAt = entity.sentAt,
                readAt = entity.readAt,
                reviewCount = entity.reviewCount,
                priorityLevel = entity.priorityLevel,
            )
    }
}

data class PagedNotificationResponse(
    @JsonProperty("content")
    val content: List<NotificationDto>,
    @JsonProperty("page")
    val page: Int,
    @JsonProperty("size")
    val size: Int,
    @JsonProperty("totalElements")
    val totalElements: Long,
    @JsonProperty("totalPages")
    val totalPages: Int,
    @JsonProperty("lastPage")
    val lastPage: Boolean,
)
