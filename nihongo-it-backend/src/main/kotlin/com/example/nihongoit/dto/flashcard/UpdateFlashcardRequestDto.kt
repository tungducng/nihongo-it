package com.example.nihongoit.dto.flashcard

import com.fasterxml.jackson.annotation.JsonProperty
import jakarta.validation.constraints.NotBlank
import jakarta.validation.constraints.Size

data class UpdateFlashcardRequestDto(
    @field:NotBlank(message = "Front text is required")
    @field:Size(max = 1000, message = "Front text must be less than 1000 characters")
    @JsonProperty("frontText")
    val frontText: String,
    
    @field:NotBlank(message = "Back text is required")
    @field:Size(max = 1000, message = "Back text must be less than 1000 characters")
    @JsonProperty("backText")
    val backText: String,
    
    @field:Size(max = 2000, message = "Notes must be less than 2000 characters")
    @JsonProperty("notes")
    val notes: String? = null,
    
    @JsonProperty("tags")
    val tags: String?
) 