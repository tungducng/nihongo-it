package com.example.japanesitlearning.dto.flashcard

import com.example.japanesitlearning.dto.ResponseDto
import com.fasterxml.jackson.annotation.JsonProperty

data class DeleteFlashcardResponseDto(
    @JsonProperty("result")
    val result: ResponseDto
) 