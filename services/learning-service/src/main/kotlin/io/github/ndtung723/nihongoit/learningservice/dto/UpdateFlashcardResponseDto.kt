package io.github.ndtung723.nihongoit.learningservice.dto

import com.fasterxml.jackson.annotation.JsonProperty

data class UpdateFlashcardResponseDto(
    @JsonProperty("data")
    val data: FlashcardDTO,
)
