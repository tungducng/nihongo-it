package com.example.nihongoit.dto.user

import com.example.nihongoit.entity.JLPTLevel
import com.fasterxml.jackson.annotation.JsonFormat
import com.fasterxml.jackson.annotation.JsonProperty
import com.fasterxml.jackson.databind.annotation.JsonDeserialize
import com.fasterxml.jackson.databind.annotation.JsonSerialize
import com.fasterxml.jackson.datatype.jsr310.deser.LocalDateTimeDeserializer
import com.fasterxml.jackson.datatype.jsr310.ser.LocalDateTimeSerializer
import java.time.LocalDateTime
import java.util.UUID

data class UserDto(
    @JsonProperty("userId")
    val userId: UUID,

    @JsonProperty("email")
    val email: String,

    @JsonProperty("fullName")
    val fullName: String,

    @JsonProperty("roleId")
    val roleId: Int,

    @JsonProperty("profilePicture")
    val profilePicture: String?,

    @JsonProperty("currentLevel")
    val currentLevel: JLPTLevel?,

    @JsonProperty("jlptGoal")
    val jlptGoal: JLPTLevel?,

    @JsonDeserialize(using = LocalDateTimeDeserializer::class)
    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd HH:mm")
    @JsonSerialize(using = LocalDateTimeSerializer::class)
    val lastLogin: LocalDateTime?,
)
