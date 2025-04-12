package com.example.nihongoit.dto.flashcard

import com.fasterxml.jackson.annotation.JsonInclude
import com.fasterxml.jackson.annotation.JsonProperty
import java.time.LocalDateTime
import java.util.UUID

@JsonInclude(JsonInclude.Include.NON_NULL)
data class FlashcardDTO(
    @JsonProperty("id")
    val id: UUID? = null,
    
    @JsonProperty("frontText")
    val frontText: String,
    
    @JsonProperty("backText")
    val backText: String,
    
    @JsonProperty("notes")
    val notes: String? = null,
    
    @JsonProperty("tags")
    val tags: List<String> = listOf(),
    
    @JsonProperty("due")
    val due: LocalDateTime? = null,
    
    @JsonProperty("reps")
    val reps: Int? = null,
    
    @JsonProperty("lapses")
    val lapses: Int? = null
)