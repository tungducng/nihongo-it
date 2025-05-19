package com.example.userservice.dto

import com.fasterxml.jackson.annotation.JsonInclude

@JsonInclude(JsonInclude.Include.NON_NULL)
data class LoginResponseDto(
    val result: ResponseType,
    val token: String? = null,
)
