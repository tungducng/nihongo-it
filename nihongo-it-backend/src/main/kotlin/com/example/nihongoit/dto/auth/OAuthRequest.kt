package com.example.nihongoit.dto.auth

import jakarta.validation.constraints.Email
import jakarta.validation.constraints.NotBlank

data class GoogleLoginRequest(
    @field:NotBlank(message = "Token ID is required")
    val tokenId: String
)

data class GoogleUserInfo(
    val email: String,
    val name: String,
    val picture: String?,
    val sub: String
) 