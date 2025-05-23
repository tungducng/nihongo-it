package com.example.userservice.dto

import com.example.userservice.entity.JlptLevel
import jakarta.validation.constraints.Email
import jakarta.validation.constraints.NotBlank
import jakarta.validation.constraints.Size

data class SignupRequest(
    @field:NotBlank(message = "Email is required")
    @field:Email(message = "Invalid email format")
    val email: String,

    @field:NotBlank(message = "Password is required")
    @field:Size(min = 6, max = 100, message = "Password must be between 6 and 100 characters")
    val password: String,

    @field:NotBlank(message = "Full name is required")
    @field:Size(max = 100, message = "Full name must not exceed 100 characters")
    val fullName: String,

    val profilePicture: String? = null,

    val currentLevel: JlptLevel = JlptLevel.N5,

    val jlptGoal: JlptLevel = JlptLevel.N3,
) 
