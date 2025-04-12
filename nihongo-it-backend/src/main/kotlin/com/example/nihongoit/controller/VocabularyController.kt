package com.example.nihongoit.controller

import com.example.nihongoit.dto.*
import com.example.nihongoit.dto.vocabulary.CreateVocabularyRequestDto
import com.example.nihongoit.dto.vocabulary.CreateVocabularyResponseDto
import com.example.nihongoit.dto.vocabulary.GetVocabularyResponseDto
import com.example.nihongoit.dto.vocabulary.PagedVocabularyResponseDto
import com.example.nihongoit.dto.vocabulary.UpdateVocabularyRequestDto
import com.example.nihongoit.dto.vocabulary.UpdateVocabularyResponseDto
import com.example.nihongoit.dto.vocabulary.VocabularyDto
import com.example.nihongoit.dto.vocabulary.VocabularyFilterRequestDto
import com.example.nihongoit.entity.JLPTLevel
import com.example.nihongoit.security.PreAuthFilter
import com.example.nihongoit.service.VocabularyService
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
import org.springframework.web.bind.annotation.*
import java.util.*

@RestController
@RequestMapping("/api/v1/vocabulary")
@Tag(name = "Vocabulary", description = "API endpoints for managing Japanese IT vocabulary, including CRUD operations and user notebook")
class VocabularyController(private val vocabularyService: VocabularyService) {

    @PreAuthFilter(hasAnyRole = ["ADMIN", "USER"])
    @PostMapping(produces = [MediaType.APPLICATION_JSON_VALUE], consumes = [MediaType.APPLICATION_JSON_VALUE])
    @ResponseStatus(HttpStatus.CREATED)
    @Operation(
        summary = "Create new vocabulary",
        description = "Creates a new vocabulary entry with provided details",
        security = [SecurityRequirement(name = "bearerAuth")]
    )
    @ApiResponses(
        value = [
            ApiResponse(
                responseCode = "201",
                description = "Vocabulary created successfully",
                content = [Content(mediaType = "application/json", schema = Schema(implementation = CreateVocabularyResponseDto::class))]
            ),
            ApiResponse(responseCode = "400", description = "Invalid request data"),
            ApiResponse(responseCode = "401", description = "Unauthorized - authentication required")
        ]
    )
    fun createVocabulary(
        @Parameter(description = "Vocabulary creation details", required = true)
        @Valid @RequestBody request: CreateVocabularyRequestDto
    ): CreateVocabularyResponseDto {
        return vocabularyService.createVocabulary(request)
    }

    @GetMapping("/{vocabId}", produces = [MediaType.APPLICATION_JSON_VALUE])
    @Operation(
        summary = "Get vocabulary by ID",
        description = "Retrieves a specific vocabulary entry by its ID"
    )
    @ApiResponses(
        value = [
            ApiResponse(
                responseCode = "200",
                description = "Successfully retrieved vocabulary",
                content = [Content(mediaType = "application/json", schema = Schema(implementation = GetVocabularyResponseDto::class))]
            ),
            ApiResponse(responseCode = "404", description = "Vocabulary not found")
        ]
    )
    fun getVocabulary(
        @Parameter(description = "Unique identifier of the vocabulary entry", required = true)
        @PathVariable vocabId: UUID
    ): GetVocabularyResponseDto {
        return vocabularyService.getVocabulary(vocabId)
    }

    @GetMapping(produces = [MediaType.APPLICATION_JSON_VALUE])
    @PreAuthFilter(hasAnyRole = ["ADMIN", "USER"])
    @Operation(
        summary = "Filter vocabulary",
        description = "Filters vocabulary entries based on various criteria with pagination support",
        security = [SecurityRequirement(name = "bearerAuth")]
    )
    @ApiResponses(
        value = [
            ApiResponse(
                responseCode = "200",
                description = "Successfully retrieved filtered vocabulary list",
                content = [Content(mediaType = "application/json", schema = Schema(implementation = PagedVocabularyResponseDto::class))]
            ),
            ApiResponse(responseCode = "401", description = "Unauthorized - authentication required")
        ]
    )
    fun filterVocabulary(
        @Parameter(description = "Filter by JLPT level")
        @RequestParam(required = false) jlptLevel: JLPTLevel?,
        
        @Parameter(description = "Filter by category")
        @RequestParam(required = false) category: String?,
        
        @Parameter(description = "Search by keyword in hiragana, kanji, or meaning")
        @RequestParam(required = false) keyword: String?,
        
        @Parameter(description = "Page number (0-based)")
        @RequestParam(defaultValue = "0") page: Int,
        
        @Parameter(description = "Page size")
        @RequestParam(defaultValue = "20") size: Int,
    ): PagedVocabularyResponseDto {
        // Validate page and size parameters to prevent invalid values
        val validPage = if (page < 0) 0 else page
        val validSize = if (size <= 0) 20 else if (size > 100) 100 else size
        
        val filter = VocabularyFilterRequestDto(
            jlptLevel = jlptLevel,
            category = category,
            keyword = keyword,
            page = validPage,
            size = validSize,
        )
        return vocabularyService.filterVocabulary(filter)
    }

    @PreAuthFilter(hasAnyRole = ["ADMIN", "USER"])
    @PutMapping("/{vocabId}", produces = [MediaType.APPLICATION_JSON_VALUE], consumes = [MediaType.APPLICATION_JSON_VALUE])
    @Operation(
        summary = "Update vocabulary",
        description = "Updates an existing vocabulary entry with the provided details",
        security = [SecurityRequirement(name = "bearerAuth")]
    )
    @ApiResponses(
        value = [
            ApiResponse(
                responseCode = "200",
                description = "Vocabulary updated successfully",
                content = [Content(mediaType = "application/json", schema = Schema(implementation = UpdateVocabularyResponseDto::class))]
            ),
            ApiResponse(responseCode = "400", description = "Invalid request data"),
            ApiResponse(responseCode = "401", description = "Unauthorized - authentication required"),
            ApiResponse(responseCode = "404", description = "Vocabulary not found")
        ]
    )
    fun updateVocabulary(
        @Parameter(description = "Unique identifier of the vocabulary to update", required = true)
        @PathVariable vocabId: UUID,
        
        @Parameter(description = "Updated vocabulary details", required = true)
        @Valid @RequestBody request: UpdateVocabularyRequestDto,
    ): UpdateVocabularyResponseDto {
        return vocabularyService.updateVocabulary(vocabId, request)
    }

    @PreAuthFilter(hasAnyRole = ["ADMIN", "USER"])
    @DeleteMapping("/{vocabId}", produces = [MediaType.APPLICATION_JSON_VALUE])
    @Operation(
        summary = "Delete vocabulary",
        description = "Deletes a vocabulary entry by its ID",
        security = [SecurityRequirement(name = "bearerAuth")]
    )
    @ApiResponses(
        value = [
            ApiResponse(
                responseCode = "200",
                description = "Vocabulary deleted successfully",
                content = [Content(mediaType = "application/json", schema = Schema(implementation = ResponseDto::class))]
            ),
            ApiResponse(responseCode = "401", description = "Unauthorized - authentication required"),
            ApiResponse(responseCode = "404", description = "Vocabulary not found")
        ]
    )
    fun deleteVocabulary(
        @Parameter(description = "Unique identifier of the vocabulary to delete", required = true)
        @PathVariable vocabId: UUID
    ): ResponseDto {
        return vocabularyService.deleteVocabulary(vocabId)
    }

    @PreAuthFilter(hasAnyRole = ["ADMIN", "USER"])
    @PostMapping("/{vocabId}/save", produces = [MediaType.APPLICATION_JSON_VALUE])
    @Operation(
        summary = "Save vocabulary to user's notebook",
        description = "Adds a vocabulary entry to the current user's personal notebook for later review",
        security = [SecurityRequirement(name = "bearerAuth")]
    )
    @ApiResponses(
        value = [
            ApiResponse(
                responseCode = "200",
                description = "Vocabulary saved to notebook successfully",
                content = [Content(mediaType = "application/json", schema = Schema(implementation = VocabularyDto::class))]
            ),
            ApiResponse(responseCode = "401", description = "Unauthorized - authentication required"),
            ApiResponse(responseCode = "404", description = "Vocabulary not found"),
            ApiResponse(responseCode = "409", description = "Vocabulary already saved to notebook")
        ]
    )
    fun saveVocabularyToNotebook(
        @Parameter(description = "Unique identifier of the vocabulary to save", required = true)
        @PathVariable vocabId: UUID
    ): VocabularyDto {
        val result = vocabularyService.saveVocabularyToNotebook(vocabId)
        return result
    }

    @PreAuthFilter(hasAnyRole = ["ADMIN", "USER"])
    @DeleteMapping("/{vocabId}/save", produces = [MediaType.APPLICATION_JSON_VALUE])
    @Operation(
        summary = "Remove vocabulary from user's notebook",
        description = "Removes a vocabulary entry from the current user's personal notebook",
        security = [SecurityRequirement(name = "bearerAuth")]
    )
    @ApiResponses(
        value = [
            ApiResponse(
                responseCode = "200",
                description = "Vocabulary removed from notebook successfully",
                content = [Content(mediaType = "application/json", schema = Schema(implementation = VocabularyDto::class))]
            ),
            ApiResponse(responseCode = "401", description = "Unauthorized - authentication required"),
            ApiResponse(responseCode = "404", description = "Vocabulary not found or not in notebook")
        ]
    )
    fun removeVocabularyFromNotebook(
        @Parameter(description = "Unique identifier of the vocabulary to remove from notebook", required = true)
        @PathVariable vocabId: UUID
    ): VocabularyDto {
        val result = vocabularyService.removeVocabularyFromNotebook(vocabId)
        return result
    }

    @PreAuthFilter(hasAnyRole = ["ADMIN", "USER"])
    @GetMapping("/saved", produces = [MediaType.APPLICATION_JSON_VALUE])
    @Operation(
        summary = "Get user's saved vocabulary",
        description = "Retrieves all vocabulary entries saved to the current user's notebook with pagination",
        security = [SecurityRequirement(name = "bearerAuth")]
    )
    @ApiResponses(
        value = [
            ApiResponse(
                responseCode = "200",
                description = "Successfully retrieved saved vocabulary",
                content = [Content(mediaType = "application/json", schema = Schema(implementation = PagedVocabularyResponseDto::class))]
            ),
            ApiResponse(responseCode = "401", description = "Unauthorized - authentication required")
        ]
    )
    fun getSavedVocabulary(
        @Parameter(description = "Page number (0-based)")
        @RequestParam(defaultValue = "0") page: Int,
        
        @Parameter(description = "Page size")
        @RequestParam(defaultValue = "20") size: Int,
    ): PagedVocabularyResponseDto {
        // Validate page and size parameters to prevent invalid values
        val validPage = if (page < 0) 0 else page
        val validSize = if (size <= 0) 20 else if (size > 100) 100 else size
        
        val filter = VocabularyFilterRequestDto(page = validPage, size = validSize)
        val result = vocabularyService.getSavedVocabulary(filter)
        return result
    }

    @GetMapping("/categories", produces = [MediaType.APPLICATION_JSON_VALUE])
    @Operation(
        summary = "Get available vocabulary categories",
        description = "Returns a list of all available categories for vocabulary entries"
    )
    @ApiResponses(
        value = [
            ApiResponse(
                responseCode = "200",
                description = "Successfully retrieved categories list",
                content = [Content(mediaType = "application/json")]
            )
        ]
    )
    fun getCategories(): List<String> {
        // Fixed list for now - could be retrieved from the database in the future
        val categories = listOf(
            "Programming",
            "Database",
            "Networking",
            "AI",
            "Cloud",
            "Mobile",
            "Web",
            "Security",
            "DevOps",
            "General IT",
        )
        return categories
    }


    @GetMapping("/jlpt-levels", produces = [MediaType.APPLICATION_JSON_VALUE])
    @Operation(
        summary = "Get available JLPT levels",
        description = "Returns a list of all available JLPT levels for vocabulary entries"
    )
    @ApiResponses(
        value = [
            ApiResponse(
                responseCode = "200",
                description = "Successfully retrieved JLPT levels list",
                content = [Content(mediaType = "application/json")]
            )
        ]
    )
    fun getJLPTLevels(): List<JLPTLevel> {
        val levels = JLPTLevel.entries
        return levels
    }
}
