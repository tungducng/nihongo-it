package com.example.nihongoit.dto.review

import com.example.nihongoit.dto.ResponseDto
import com.example.nihongoit.dto.flashcard.FlashcardDTO
import com.fasterxml.jackson.annotation.JsonProperty

data class ReviewFlashcardResponseDto(
    @JsonProperty("result")
    val result: ResponseDto,
    
    @JsonProperty("data")
    val data: FlashcardDTO
) 