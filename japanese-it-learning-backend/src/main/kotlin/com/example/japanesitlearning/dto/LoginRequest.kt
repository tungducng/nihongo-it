package com.example.japanesitlearning.dto
import com.fasterxml.jackson.annotation.JsonCreator
import com.fasterxml.jackson.annotation.JsonProperty
import jakarta.validation.constraints.Email
import jakarta.validation.constraints.NotBlank
import jakarta.validation.constraints.Size


data class LoginRequest(
    @field:NotBlank(message = "Username must not be blank")
    @field:Size(min = 3, max = 50, message = "Username must be between 3 and 50 characters")
    @JsonProperty("username")
    val username: String,

    @field:NotBlank(message = "Password must not be blank")
    @field:Size(min = 6, max = 100, message = "Password must be between 6 and 100 characters")
    @JsonProperty("password")
    val password: String
)

data class SignupRequest @JsonCreator constructor(
    @field:NotBlank(message = "Username must not be blank")
    @field:Size(min = 3, max = 50, message = "Username must be between 3 and 50 characters")
    @JsonProperty("username")
    val username: String,

    @field:NotBlank(message = "Email must not be blank")
    @field:Email(message = "Email must be a valid email address")
    @field:Size(max = 100, message = "Email must not exceed 100 characters")
    @JsonProperty("email")
    val email: String,

    @field:NotBlank(message = "Password must not be blank")
    @field:Size(min = 6, max = 100, message = "Password must be between 6 and 100 characters")
    @JsonProperty("password")
    val password: String,

    @JsonProperty("roleId")
    val roleId: Int? = 2 // Default is ROLE_USER
)

data class MessageResponse(val message: String)