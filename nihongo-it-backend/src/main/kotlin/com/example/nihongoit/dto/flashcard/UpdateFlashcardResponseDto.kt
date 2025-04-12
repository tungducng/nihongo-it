package com.example.nihongoit.dto.flashcard

import com.example.nihongoit.dto.ResponseDto
import com.fasterxml.jackson.annotation.JsonProperty

data class UpdateFlashcardResponseDto(
    @JsonProperty("result")
    val result: ResponseDto,
    
    @JsonProperty("data")
    val data: FlashcardDTO
) 