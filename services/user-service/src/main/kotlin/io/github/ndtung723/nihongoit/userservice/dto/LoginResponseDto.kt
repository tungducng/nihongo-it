package io.github.ndtung723.nihongoit.userservice.dto

import com.fasterxml.jackson.annotation.JsonInclude

@JsonInclude(JsonInclude.Include.NON_NULL)
data class LoginResponseDto(
    val token: String,
    val refreshToken: String? = null,
)
