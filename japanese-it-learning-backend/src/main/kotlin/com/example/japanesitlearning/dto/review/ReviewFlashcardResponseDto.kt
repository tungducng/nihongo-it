package com.example.japanesitlearning.dto.review

import com.example.japanesitlearning.dto.ResponseDto
import com.example.japanesitlearning.dto.flashcard.FlashcardDTO
import com.fasterxml.jackson.annotation.JsonProperty

data class ReviewFlashcardResponseDto(
    @JsonProperty("result")
    val result: ResponseDto,
    
    @JsonProperty("data")
    val data: FlashcardDTO
) 