package com.example.nihongoit.dto.auth

import com.example.nihongoit.dto.ResponseType
import com.fasterxml.jackson.annotation.JsonInclude

@JsonInclude(JsonInclude.Include.NON_NULL)
data class LoginResponseDto(
    val result: ResponseType,
    val token: String? = null,
)
