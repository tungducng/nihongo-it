package com.example.nihongoit.dto.flashcard

import com.fasterxml.jackson.annotation.JsonProperty
import jakarta.validation.constraints.NotBlank
import jakarta.validation.constraints.Size
import java.util.UUID

data class CreateFlashcardRequestDto(
    @JsonProperty("frontText")
    val frontText: String ?= null,
    
    @JsonProperty("backText")
    val backText: String ?= null,
    
    @JsonProperty("vocabularyId")
    val vocabularyId: UUID? = null
) 