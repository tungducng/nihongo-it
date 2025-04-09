package com.example.japanesitlearning.dto.auth

import com.example.japanesitlearning.dto.ResponseDto
import com.fasterxml.jackson.annotation.JsonInclude

@JsonInclude(JsonInclude.Include.NON_NULL)
data class SignupResponseDto(
    val result: ResponseDto,
)
