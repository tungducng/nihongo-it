package com.example.userservice.dto

import com.fasterxml.jackson.annotation.JsonProperty

data class DeleteFlashcardResponseDto(
    @JsonProperty("result")
    val result: ResponseDto
) 