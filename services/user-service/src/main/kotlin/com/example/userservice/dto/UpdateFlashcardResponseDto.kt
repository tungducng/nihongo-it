package com.example.userservice.dto

import com.fasterxml.jackson.annotation.JsonProperty

data class UpdateFlashcardResponseDto(
    @JsonProperty("result")
    val result: ResponseDto,
    
    @JsonProperty("data")
    val data: FlashcardDTO
) 