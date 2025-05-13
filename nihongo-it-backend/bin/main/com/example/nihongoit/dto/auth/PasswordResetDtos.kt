package com.example.nihongoit.dto.auth

import com.example.nihongoit.dto.ResponseType
import jakarta.validation.constraints.Email
import jakarta.validation.constraints.NotBlank
import jakarta.validation.constraints.Size

/**
 * DTO for requesting a password reset
 */
data class PasswordResetRequestDto(
    @field:NotBlank(message = "Email is required")
    @field:Email(message = "Invalid email format")
    val email: String
)

/**
 * DTO for password reset response
 */
data class PasswordResetResponseDto(
    val status: ResponseType,
    val message: String
)

/**
 * DTO for performing the actual password reset
 */
data class ResetPasswordDto(
    @field:NotBlank(message = "Token is required")
    val token: String,
    
    @field:NotBlank(message = "Password is required")
    @field:Size(min = 8, message = "Password must be at least 8 characters")
    val password: String,
    
    @field:NotBlank(message = "Password confirmation is required")
    val confirmPassword: String
) 