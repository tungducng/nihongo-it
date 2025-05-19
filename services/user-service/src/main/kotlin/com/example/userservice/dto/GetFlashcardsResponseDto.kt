package com.example.userservice.dto

import com.fasterxml.jackson.annotation.JsonProperty

data class GetFlashcardsResponseDto(
    @JsonProperty("result")
    val result: ResponseDto,
    
    @JsonProperty("data")
    val data: List<FlashcardDTO>
) 