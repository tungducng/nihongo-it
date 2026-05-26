package io.github.ndtung723.nihongoit.learningservice.service

import io.github.ndtung723.nihongoit.learningservice.dto.FlashcardDTO
import io.github.ndtung723.nihongoit.learningservice.entity.FlashcardEntity

fun FlashcardEntity.toDto(): FlashcardDTO =
    FlashcardDTO(
        id = flashcardId,
        frontText = frontText,
        backText = backText,
        vocabularyId = vocabulary?.vocabId,
        due = due,
        reps = reps,
        lapses = lapses,
        state =
            FSRSService.State.entries
                .find { it.value == state }
                ?.name
                ?.lowercase() ?: "new",
        difficulty = difficulty,
        stability = stability,
        interval = scheduledDays,
        createdAt = createdAt,
        updatedAt = updatedAt,
    )
