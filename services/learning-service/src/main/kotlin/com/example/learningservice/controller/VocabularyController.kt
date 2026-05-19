package com.example.learningservice.controller

import com.example.learningservice.dto.CategoryDTO
import com.example.learningservice.dto.CreateVocabularyRequestDto
import com.example.learningservice.dto.PagedVocabularyResponseDto
import com.example.learningservice.dto.TopicDTO
import com.example.learningservice.dto.UpdateVocabularyRequestDto
import com.example.learningservice.dto.VocabularyDto
import com.example.learningservice.dto.VocabularyFilterRequestDto
import com.example.learningservice.entity.JlptLevel
import com.example.learningservice.service.CategoryService
import com.example.learningservice.service.VocabularyService
import io.swagger.v3.oas.annotations.Operation
import io.swagger.v3.oas.annotations.Parameter
import io.swagger.v3.oas.annotations.media.Content
import io.swagger.v3.oas.annotations.media.Schema
import io.swagger.v3.oas.annotations.responses.ApiResponse
import io.swagger.v3.oas.annotations.responses.ApiResponses
import io.swagger.v3.oas.annotations.security.SecurityRequirement
import io.swagger.v3.oas.annotations.tags.Tag
import jakarta.validation.Valid
import org.springframework.http.HttpStatus
import org.springframework.http.MediaType
import org.springframework.http.ResponseEntity
import org.springframework.security.access.prepost.PreAuthorize
import org.springframework.web.bind.annotation.*
import java.util.*

@RestController
@RequestMapping("/api/v1/learning/vocabulary")
@Tag(name = "Vocabulary", description = "API endpoints for managing Japanese IT vocabulary, including CRUD operations and user notebook")
class VocabularyController(
    private val vocabularyService: VocabularyService,
    private val categoryService: CategoryService,
) {
    @PreAuthorize("hasAnyRole('USER', 'ADMIN')")
    @PostMapping(produces = [MediaType.APPLICATION_JSON_VALUE], consumes = [MediaType.APPLICATION_JSON_VALUE])
    @ResponseStatus(HttpStatus.CREATED)
    @Operation(
        summary = "Create new vocabulary",
        description = "Creates a new vocabulary entry with provided details",
        security = [SecurityRequirement(name = "bearerAuth")],
    )
    @ApiResponses(
        value = [
            ApiResponse(
                responseCode = "201",
                description = "Vocabulary created successfully",
                content = [Content(mediaType = "application/json", schema = Schema(implementation = VocabularyDto::class))],
            ),
            ApiResponse(responseCode = "400", description = "Invalid request data"),
            ApiResponse(responseCode = "401", description = "Unauthorized - authentication required"),
        ],
    )
    fun createVocabulary(
        @Parameter(description = "Vocabulary creation details", required = true)
        @Valid
        @RequestBody request: CreateVocabularyRequestDto,
    ): VocabularyDto =
        requireNotNull(vocabularyService.createVocabulary(request).data) {
            "Created vocabulary payload missing"
        }

    @GetMapping("/{vocabId}", produces = [MediaType.APPLICATION_JSON_VALUE])
    @Operation(
        summary = "Get vocabulary by ID",
        description = "Retrieves a specific vocabulary entry by its ID",
    )
    fun getVocabulary(
        @Parameter(description = "Unique identifier of the vocabulary entry", required = true)
        @PathVariable vocabId: UUID,
    ): VocabularyDto = vocabularyService.getVocabularybyId(vocabId).data

    @GetMapping("/term/{term}", produces = [MediaType.APPLICATION_JSON_VALUE])
    @Operation(
        summary = "Get vocabulary by term",
        description = "Retrieves a specific vocabulary entry by its Japanese term",
    )
    fun getVocabularyByTerm(
        @Parameter(description = "Japanese term of the vocabulary entry", required = true)
        @PathVariable term: String,
    ): VocabularyDto = vocabularyService.getVocabularyByTerm(term).data

    @GetMapping(produces = [MediaType.APPLICATION_JSON_VALUE])
    @PreAuthorize("hasAnyRole('USER', 'ADMIN')")
    @Operation(
        summary = "Filter vocabulary",
        description = "Filters vocabulary entries based on various criteria with pagination support",
        security = [SecurityRequirement(name = "bearerAuth")],
    )
    fun filterVocabulary(
        @RequestParam(required = false) jlptLevel: JlptLevel?,
        @RequestParam(required = false) topicName: String?,
        @RequestParam(required = false) keyword: String?,
        @RequestParam(defaultValue = "0") page: Int,
        @RequestParam(defaultValue = "20") size: Int,
        @RequestParam(required = false) sort: String?,
    ): PagedVocabularyResponseDto {
        val validPage = if (page < 0) 0 else page
        val validSize =
            if (size <= 0) {
                20
            } else if (size > 100) {
                100
            } else {
                size
            }

        val filter =
            VocabularyFilterRequestDto(
                jlptLevel = jlptLevel,
                topicName = topicName,
                keyword = keyword,
                page = validPage,
                size = validSize,
                sort = sort,
            )
        return vocabularyService.filterVocabulary(filter)
    }

    @PreAuthorize("hasAnyRole('USER', 'ADMIN')")
    @PutMapping("/{vocabId}", produces = [MediaType.APPLICATION_JSON_VALUE], consumes = [MediaType.APPLICATION_JSON_VALUE])
    @Operation(
        summary = "Update vocabulary",
        description = "Updates an existing vocabulary entry with the provided details",
        security = [SecurityRequirement(name = "bearerAuth")],
    )
    fun updateVocabulary(
        @PathVariable vocabId: UUID,
        @Valid @RequestBody request: UpdateVocabularyRequestDto,
    ): VocabularyDto = vocabularyService.updateVocabulary(vocabId, request).data

    @PreAuthorize("hasAnyRole('USER', 'ADMIN')")
    @DeleteMapping("/{vocabId}", produces = [MediaType.APPLICATION_JSON_VALUE])
    @Operation(
        summary = "Delete vocabulary",
        description = "Deletes a vocabulary entry by its ID",
        security = [SecurityRequirement(name = "bearerAuth")],
    )
    fun deleteVocabulary(
        @PathVariable vocabId: UUID,
    ): ResponseEntity<Void> {
        vocabularyService.deleteVocabulary(vocabId)
        return ResponseEntity.noContent().build()
    }

    @PreAuthorize("hasAnyRole('USER', 'ADMIN')")
    @PostMapping("/{vocabId}/save", produces = [MediaType.APPLICATION_JSON_VALUE])
    @Operation(
        summary = "Save vocabulary to user's notebook by ID",
        description = "Adds a vocabulary entry to the current user's personal notebook using ID (legacy method)",
        security = [SecurityRequirement(name = "bearerAuth")],
    )
    fun saveVocabularyToNotebook(
        @PathVariable vocabId: UUID,
    ): VocabularyDto = vocabularyService.saveVocabularyToNotebook(vocabId)

    @PreAuthorize("hasAnyRole('USER', 'ADMIN')")
    @DeleteMapping("/{vocabId}/save", produces = [MediaType.APPLICATION_JSON_VALUE])
    @Operation(
        summary = "Remove vocabulary from user's notebook",
        description = "Removes a vocabulary entry from the current user's personal notebook",
    )
    fun removeVocabularyFromNotebook(
        @PathVariable vocabId: UUID,
    ): VocabularyDto = vocabularyService.removeVocabularyFromNotebook(vocabId)

    @PreAuthorize("hasAnyRole('USER', 'ADMIN')")
    @GetMapping("/saved", produces = [MediaType.APPLICATION_JSON_VALUE])
    @Operation(
        summary = "Get user's saved vocabulary",
        description = "Retrieves all vocabulary entries saved to the current user's notebook with pagination",
        security = [SecurityRequirement(name = "bearerAuth")],
    )
    fun getSavedVocabulary(
        @RequestParam(required = false) keyword: String?,
        @RequestParam(defaultValue = "0") page: Int,
        @RequestParam(defaultValue = "20") size: Int,
        @RequestParam(defaultValue = "date_desc") sort: String?,
    ): PagedVocabularyResponseDto {
        val validPage = if (page < 0) 0 else page
        val validSize =
            if (size <= 0) {
                20
            } else if (size > 100) {
                100
            } else {
                size
            }

        val filter =
            VocabularyFilterRequestDto(
                keyword = keyword,
                page = validPage,
                size = validSize,
            )
        return vocabularyService.getSavedVocabulary(filter)
    }

    @GetMapping("/topics", produces = [MediaType.APPLICATION_JSON_VALUE])
    @Operation(
        summary = "Get all topics for vocabulary",
        description = "Returns a list of all available topics for organizing vocabulary entries",
    )
    fun getAllTopics(): ResponseEntity<List<TopicDTO>> = ResponseEntity.ok(categoryService.getAllTopics())

    @GetMapping("/categories", produces = [MediaType.APPLICATION_JSON_VALUE])
    @Operation(
        summary = "Get available vocabulary categories",
        description = "Returns a list of all available categories for vocabulary entries",
    )
    fun getCategories(): List<CategoryDTO> = categoryService.getAllCategories()

    @GetMapping("/jlpt-levels", produces = [MediaType.APPLICATION_JSON_VALUE])
    @Operation(
        summary = "Get available JLPT levels",
        description = "Returns a list of all available JLPT levels for vocabulary entries",
    )
    fun getJlptLevels(): List<JlptLevel> = JlptLevel.entries

    @GetMapping("/categories/{categoryId}/topics", produces = [MediaType.APPLICATION_JSON_VALUE])
    @Operation(
        summary = "Get topics for a category",
        description = "Returns all topics belonging to a specific category",
    )
    fun getTopicsByCategory(
        @PathVariable categoryId: UUID,
    ): ResponseEntity<List<TopicDTO>> = ResponseEntity.ok(categoryService.getTopicsForCategory(categoryId))
}
