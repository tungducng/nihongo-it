package com.example.japanesitlearning.controller

import com.example.japanesitlearning.dto.vocabulary.CreateVocabularyRequest
import com.example.japanesitlearning.dto.vocabulary.UpdateVocabularyRequest
import com.example.japanesitlearning.dto.vocabulary.VocabularyPageResponse
import com.example.japanesitlearning.dto.vocabulary.VocabularyResponse
import com.example.japanesitlearning.service.VocabularyService
import io.swagger.v3.oas.annotations.Operation
import io.swagger.v3.oas.annotations.Parameter
import io.swagger.v3.oas.annotations.tags.Tag
import org.springframework.data.domain.PageRequest
import org.springframework.data.domain.Sort
import org.springframework.http.HttpStatus
import org.springframework.http.MediaType
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.DeleteMapping
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.PutMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RequestParam
import org.springframework.web.bind.annotation.RestController
import java.util.UUID

@RestController
@RequestMapping("/api/vocabulary")
@Tag(name = "Vocabulary", description = "API for vocabulary CRUD operations")
class VocabularyController(
    private val vocabularyService: VocabularyService,
) {

    @PostMapping
    @Operation(summary = "Create vocabulary", description = "Creates a new vocabulary item")
    fun createVocabulary(@RequestBody request: CreateVocabularyRequest): ResponseEntity<VocabularyResponse> {
        val created = vocabularyService.createVocabulary(request)
        return ResponseEntity.status(HttpStatus.CREATED).body(created)
    }

    @GetMapping("/{id}")
    @Operation(summary = "Get vocabulary", description = "Get vocabulary by ID")
    fun getVocabulary(
        @PathVariable @Parameter(description = "Vocabulary ID") id: UUID
    ): ResponseEntity<VocabularyResponse> {
        val vocabulary = vocabularyService.getVocabulary(id)
        return ResponseEntity.ok(vocabulary)
    }

    @GetMapping
    @Operation(summary = "List vocabularies", description = "Get paginated list of vocabularies")
    fun getAllVocabularies(
        @RequestParam(defaultValue = "0") @Parameter(description = "Page number") page: Int,
        @RequestParam(defaultValue = "20") @Parameter(description = "Page size") size: Int,
        @RequestParam(defaultValue = "hiragana") @Parameter(description = "Sort by field") sortBy: String,
        @RequestParam(defaultValue = "asc") @Parameter(description = "Sort direction") sortDir: String
    ): ResponseEntity<VocabularyPageResponse> {
        val direction = if (sortDir.equals("desc", ignoreCase = true))
            Sort.Direction.DESC else Sort.Direction.ASC
        val pageable = PageRequest.of(page, size, Sort.by(direction, sortBy))
        val result = vocabularyService.getAllVocabularies(pageable)
        return ResponseEntity.ok(result)
    }

    @PutMapping("/{id}")
    @Operation(summary = "Update vocabulary", description = "Update vocabulary by ID")
    fun updateVocabulary(
        @PathVariable @Parameter(description = "Vocabulary ID") id: UUID,
        @RequestBody request: UpdateVocabularyRequest
    ): ResponseEntity<VocabularyResponse> {
        val updated = vocabularyService.updateVocabulary(id, request)
        return ResponseEntity.ok(updated)
    }

    @DeleteMapping("/{id}")
    @Operation(summary = "Delete vocabulary", description = "Delete vocabulary by ID")
    fun deleteVocabulary(
        @PathVariable @Parameter(description = "Vocabulary ID") id: UUID
    ): ResponseEntity<Void> {
        vocabularyService.deleteVocabulary(id)
        return ResponseEntity.noContent().build()
    }

    @GetMapping("/search")
    @Operation(summary = "Search vocabularies", description = "Search vocabularies by keyword")
    fun searchVocabularies(
        @RequestParam @Parameter(description = "Search keyword") keyword: String,
        @RequestParam(defaultValue = "0") page: Int,
        @RequestParam(defaultValue = "20") size: Int
    ): ResponseEntity<VocabularyPageResponse> {
        val pageable = PageRequest.of(page, size)
        val result = vocabularyService.searchVocabularies(keyword, pageable)
        return ResponseEntity.ok(result)
    }

    @GetMapping("/jlpt-level/{level}")
    @Operation(summary = "Get vocabularies by JLPT level", description = "List vocabularies filtered by JLPT level")
    fun getVocabulariesByJlptLevel(
        @PathVariable @Parameter(description = "JLPT level (1-5)") level: Int,
        @RequestParam(defaultValue = "0") page: Int,
        @RequestParam(defaultValue = "20") size: Int
    ): ResponseEntity<VocabularyPageResponse> {
        val pageable = PageRequest.of(page, size)
        val result = vocabularyService.getVocabulariesByJlptLevel(level, pageable)
        return ResponseEntity.ok(result)
    }

    @GetMapping("/it")
    @Operation(summary = "Get IT vocabularies", description = "List vocabularies with IT context")
    fun getItVocabularies(
        @RequestParam(defaultValue = "0") page: Int,
        @RequestParam(defaultValue = "20") size: Int
    ): ResponseEntity<VocabularyPageResponse> {
        val pageable = PageRequest.of(page, size)
        val result = vocabularyService.getItVocabularies(pageable)
        return ResponseEntity.ok(result)
    }

    @GetMapping("/by-lesson/{lessonId}")
    @Operation(summary = "Get vocabularies by lesson", description = "List vocabularies by lesson ID")
    fun getVocabulariesByLesson(
        @PathVariable @Parameter(description = "Lesson ID") lessonId: UUID,
        @RequestParam(defaultValue = "0") page: Int,
        @RequestParam(defaultValue = "20") size: Int
    ): ResponseEntity<VocabularyPageResponse> {
        val pageable = PageRequest.of(page, size)
        val result = vocabularyService.getVocabulariesByLesson(lessonId, pageable)
        return ResponseEntity.ok(result)
    }
}