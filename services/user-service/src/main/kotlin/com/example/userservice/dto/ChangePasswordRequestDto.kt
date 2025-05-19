package com.example.userservice.dto

import jakarta.validation.constraints.NotBlank
import jakarta.validation.constraints.Size

/**
 * DTO for changing user password
 */
data class ChangePasswordRequestDto(
    @get:NotBlank(message = "Current password is required")
    val currentPassword: String,
    
    @get:NotBlank(message = "New password is required")
    @get:Size(min = 8, message = "New password must be at least 8 characters")
    val newPassword: String,
    
    @get:NotBlank(message = "Password confirmation is required")
    val confirmPassword: String
) 