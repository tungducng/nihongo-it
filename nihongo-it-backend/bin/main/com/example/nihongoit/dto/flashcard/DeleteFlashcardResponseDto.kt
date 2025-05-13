package com.example.nihongoit.dto.flashcard

import com.example.nihongoit.dto.ResponseDto
import com.fasterxml.jackson.annotation.JsonProperty

data class DeleteFlashcardResponseDto(
    @JsonProperty("result")
    val result: ResponseDto
) 