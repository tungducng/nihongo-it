package com.example.learningservice.dto

import com.example.learningservice.entity.UserProgressEntity
import com.fasterxml.jackson.annotation.JsonInclude
import com.fasterxml.jackson.annotation.JsonProperty
import java.time.LocalDateTime
import java.util.UUID

@JsonInclude(JsonInclude.Include.NON_NULL)
data class UserProgressDto(
    @JsonProperty("userId")
    val userId: UUID,
    @JsonProperty("streakCount")
    val streakCount: Int,
    @JsonProperty("lastStudyDate")
    val lastStudyDate: LocalDateTime?,
    @JsonProperty("points")
    val points: Int,
    @JsonProperty("dailyGoalMinutes")
    val dailyGoalMinutes: Int,
) {
    companion object {
        fun fromEntity(entity: UserProgressEntity): UserProgressDto =
            UserProgressDto(
                userId = entity.userId,
                streakCount = entity.streakCount,
                lastStudyDate = entity.lastStudyDate,
                points = entity.points,
                dailyGoalMinutes = entity.dailyGoalMinutes,
            )
    }
}
