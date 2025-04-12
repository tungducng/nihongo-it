package com.example.nihongoit.dto.flashcard

import com.example.nihongoit.dto.ResponseDto
import com.fasterxml.jackson.annotation.JsonProperty

data class GetDueCardsResponseDto(
    @JsonProperty("result")
    val result: ResponseDto,
    
    @JsonProperty("data")
    val data: List<FlashcardDTO>
) 