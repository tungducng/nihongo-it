package com.example.userservice.dto

import com.fasterxml.jackson.annotation.JsonProperty
import jakarta.validation.constraints.Email
import jakarta.validation.constraints.NotBlank

data class LoginRequest(
    @JsonProperty(value = "email")
    @field:NotBlank(message = "Email is required")
    @field:Email(message = "Invalid email format")
    val email: String,

    @JsonProperty(value = "password")
    @field:NotBlank(message = "Password is required")
    val password: String,
)
