//package com.example.nihongoit.controller
//
//import com.example.nihongoit.dto.flashcard.*
//import com.example.nihongoit.dto.review.ReviewFlashcardResponseDto
//import com.example.nihongoit.dto.review.ReviewRequest
//import com.example.nihongoit.service.FlashcardService
//import com.example.nihongoit.util.UserAuthUtil
//import io.swagger.v3.oas.annotations.Operation
//import io.swagger.v3.oas.annotations.Parameter
//import io.swagger.v3.oas.annotations.media.Content
//import io.swagger.v3.oas.annotations.media.Schema
//import io.swagger.v3.oas.annotations.responses.ApiResponse
//import io.swagger.v3.oas.annotations.responses.ApiResponses
//import io.swagger.v3.oas.annotations.tags.Tag
//import jakarta.validation.Valid
//import org.springframework.http.HttpStatus
//import org.springframework.http.MediaType
//import org.springframework.web.bind.annotation.*
//import java.util.*
//
//@RestController
//@RequestMapping("/api/v1/flashcards", produces = [MediaType.APPLICATION_JSON_VALUE])
//@Tag(name = "Flashcard", description = "Flashcard management endpoints for spaced repetition study")
//class FlashcardController(
//    private val flashcardService: FlashcardService,
//    private val userAuthUtil: UserAuthUtil
//) {
//
//    @GetMapping(
//        "/due",
//        produces = [MediaType.APPLICATION_JSON_VALUE]
//    )
//    @Operation(
//        summary = "Get due flashcards",
//        description = "Retrieves all flashcards that are due for review based on the FSRS algorithm"
//    )
//    @ApiResponses(
//        value = [
//            ApiResponse(
//                responseCode = "200",
//                description = "Successfully retrieved due flashcards",
//                content = [Content(mediaType = "application/json", schema = Schema(implementation = GetDueCardsResponseDto::class))]
//            ),
//            ApiResponse(responseCode = "401", description = "Unauthorized - authentication required")
//        ]
//    )
//    fun getDueCards(): GetDueCardsResponseDto {
//        return flashcardService.getDueCards()
//    }
//
//    @GetMapping(
//        produces = [MediaType.APPLICATION_JSON_VALUE]
//    )
//    @Operation(
//        summary = "Get all flashcards",
//        description = "Retrieves all flashcards belonging to the current user"
//    )
//    @ApiResponses(
//        value = [
//            ApiResponse(
//                responseCode = "200",
//                description = "Successfully retrieved all flashcards",
//                content = [Content(mediaType = "application/json", schema = Schema(implementation = GetFlashcardsResponseDto::class))]
//            ),
//            ApiResponse(responseCode = "401", description = "Unauthorized - authentication required")
//        ]
//    )
//    fun getAllFlashcards(): GetFlashcardsResponseDto {
//        return flashcardService.getAllFlashcards()
//    }
//
//    @GetMapping(
//        "/{id}",
//        produces = [MediaType.APPLICATION_JSON_VALUE]
//    )
//    @Operation(
//        summary = "Get flashcard by ID",
//        description = "Retrieves a specific flashcard by its unique identifier"
//    )
//    @ApiResponses(
//        value = [
//            ApiResponse(
//                responseCode = "200",
//                description = "Successfully retrieved the flashcard",
//                content = [Content(mediaType = "application/json", schema = Schema(implementation = GetFlashcardResponseDto::class))]
//            ),
//            ApiResponse(responseCode = "401", description = "Unauthorized - authentication required"),
//            ApiResponse(responseCode = "404", description = "Flashcard not found")
//        ]
//    )
//    fun getFlashcardById(
//        @Parameter(description = "Unique identifier of the flashcard", required = true)
//        @PathVariable id: UUID
//    ): GetFlashcardResponseDto {
//        return flashcardService.getFlashcardById(id)
//    }
//
//    @PostMapping(
//        produces = [MediaType.APPLICATION_JSON_VALUE],
//        consumes = [MediaType.APPLICATION_JSON_VALUE]
//    )
//    @ResponseStatus(HttpStatus.CREATED)
//    @Operation(
//        summary = "Create new flashcard",
//        description = "Creates a new flashcard with the provided details"
//    )
//    @ApiResponses(
//        value = [
//            ApiResponse(
//                responseCode = "201",
//                description = "Flashcard created successfully",
//                content = [Content(mediaType = "application/json", schema = Schema(implementation = CreateFlashcardResponseDto::class))]
//            ),
//            ApiResponse(responseCode = "400", description = "Invalid request data"),
//            ApiResponse(responseCode = "401", description = "Unauthorized - authentication required")
//        ]
//    )
//    fun createFlashcard(
//        @Parameter(description = "Flashcard creation details", required = true)
//        @Valid @RequestBody request: CreateFlashcardRequestDto
//    ): CreateFlashcardResponseDto {
//        return flashcardService.createFlashcard(request)
//    }
//
//    @PutMapping(
//        "/{id}",
//        produces = [MediaType.APPLICATION_JSON_VALUE],
//        consumes = [MediaType.APPLICATION_JSON_VALUE]
//    )
//    @Operation(
//        summary = "Update flashcard",
//        description = "Updates an existing flashcard with the provided details"
//    )
//    @ApiResponses(
//        value = [
//            ApiResponse(
//                responseCode = "200",
//                description = "Flashcard updated successfully",
//                content = [Content(mediaType = "application/json", schema = Schema(implementation = UpdateFlashcardResponseDto::class))]
//            ),
//            ApiResponse(responseCode = "400", description = "Invalid request data"),
//            ApiResponse(responseCode = "401", description = "Unauthorized - authentication required"),
//            ApiResponse(responseCode = "404", description = "Flashcard not found")
//        ]
//    )
//    fun updateFlashcard(
//        @Parameter(description = "Unique identifier of the flashcard to update", required = true)
//        @PathVariable id: UUID,
//        @Parameter(description = "Updated flashcard details", required = true)
//        @Valid @RequestBody request: UpdateFlashcardRequestDto
//    ): UpdateFlashcardResponseDto {
//        return flashcardService.updateFlashcard(id, request)
//    }
//
//    @DeleteMapping(
//        "/{id}",
//        produces = [MediaType.APPLICATION_JSON_VALUE]
//    )
//    @Operation(
//        summary = "Delete flashcard",
//        description = "Deletes a flashcard by its unique identifier"
//    )
//    @ApiResponses(
//        value = [
//            ApiResponse(
//                responseCode = "200",
//                description = "Flashcard deleted successfully",
//                content = [Content(mediaType = "application/json", schema = Schema(implementation = DeleteFlashcardResponseDto::class))]
//            ),
//            ApiResponse(responseCode = "401", description = "Unauthorized - authentication required"),
//            ApiResponse(responseCode = "404", description = "Flashcard not found")
//        ]
//    )
//    fun deleteFlashcard(
//        @Parameter(description = "Unique identifier of the flashcard to delete", required = true)
//        @PathVariable id: UUID
//    ): DeleteFlashcardResponseDto {
//        return flashcardService.deleteFlashcard(id)
//    }
//
//    @PostMapping(
//        "/{id}/review",
//        produces = [MediaType.APPLICATION_JSON_VALUE],
//        consumes = [MediaType.APPLICATION_JSON_VALUE]
//    )
//    @Operation(
//        summary = "Review flashcard",
//        description = "Records a review for a flashcard with the specified difficulty rating (0-4)"
//    )
//    @ApiResponses(
//        value = [
//            ApiResponse(
//                responseCode = "200",
//                description = "Flashcard reviewed successfully",
//                content = [Content(mediaType = "application/json", schema = Schema(implementation = ReviewFlashcardResponseDto::class))]
//            ),
//            ApiResponse(responseCode = "400", description = "Invalid rating value"),
//            ApiResponse(responseCode = "401", description = "Unauthorized - authentication required"),
//            ApiResponse(responseCode = "404", description = "Flashcard not found")
//        ]
//    )
//    fun reviewFlashcard(
//        @Parameter(description = "Unique identifier of the flashcard to review", required = true)
//        @PathVariable id: UUID,
//        @Parameter(description = "Rating details (0-4 where 0 is hardest, 4 is easiest)", required = true)
//        @Valid @RequestBody reviewRequest: ReviewRequest
//    ): ReviewFlashcardResponseDto {
//        return flashcardService.processReview(id, reviewRequest.rating)
//    }
//
//    @GetMapping(
//        "/statistics",
//        produces = [MediaType.APPLICATION_JSON_VALUE]
//    )
//    @Operation(
//        summary = "Get study statistics",
//        description = "Retrieves statistics about the user's flashcard study progress"
//    )
//    @ApiResponses(
//        value = [
//            ApiResponse(
//                responseCode = "200",
//                description = "Statistics retrieved successfully",
//                content = [Content(mediaType = "application/json", schema = Schema(implementation = GetStatisticsResponseDto::class))]
//            ),
//            ApiResponse(responseCode = "401", description = "Unauthorized - authentication required")
//        ]
//    )
//    fun getStudyStatistics(): GetStatisticsResponseDto {
//        return flashcardService.getStudyStatistics()
//    }
//}