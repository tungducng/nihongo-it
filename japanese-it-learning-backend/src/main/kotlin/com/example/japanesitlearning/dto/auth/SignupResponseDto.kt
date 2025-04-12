package com.example.japanesitlearning.dto.auth

import com.example.japanesitlearning.dto.ResponseDto
import com.example.japanesitlearning.dto.ResponseType
import com.fasterxml.jackson.annotation.JsonInclude

@JsonInclude(JsonInclude.Include.NON_NULL)
data class SignupResponseDto(
    val status: ResponseType,
    val message: String? = null
) {
    constructor(responseDto: ResponseDto) : this(
        status = responseDto.status,
        message = responseDto.message
    )
}
