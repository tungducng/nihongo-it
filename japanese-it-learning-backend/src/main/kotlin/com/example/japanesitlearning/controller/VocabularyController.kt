package com.example.japanesitlearning.controller

import com.example.japanesitlearning.dto.*
import com.example.japanesitlearning.dto.vocabulary.CreateVocabularyRequestDto
import com.example.japanesitlearning.dto.vocabulary.PagedVocabularyResponseDto
import com.example.japanesitlearning.dto.vocabulary.VocabularyDto
import com.example.japanesitlearning.dto.vocabulary.VocabularyFilterRequestDto
import com.example.japanesitlearning.entity.JLPTLevel
import com.example.japanesitlearning.security.PreAuthFilter
import com.example.japanesitlearning.service.VocabularyService
import jakarta.validation.Valid
import org.springframework.web.bind.annotation.*
import java.util.*

@RestController
@RequestMapping("/api/v1/vocabulary")
class VocabularyController(private val vocabularyService: VocabularyService) {

    @PreAuthFilter(hasAnyRole = ["ADMIN", "USER"])
    @PostMapping
    fun createVocabulary(@Valid @RequestBody request: CreateVocabularyRequestDto): VocabularyDto {
        val result = vocabularyService.createVocabulary(request)
        return result
    }

    @GetMapping("/{vocabId}")
    fun getVocabulary(@PathVariable vocabId: UUID): VocabularyDto {
        val result = vocabularyService.getVocabulary(vocabId)
        return result
    }

    @PreAuthFilter(hasAnyRole = ["ADMIN"])
    @GetMapping
    fun filterVocabulary(
        @RequestParam(required = false) jlptLevel: JLPTLevel?,
        @RequestParam(required = false) category: String?,
        @RequestParam(required = false) contentType: String?,
        @RequestParam(required = false) keyword: String?,
        @RequestParam(defaultValue = "0") page: Int,
        @RequestParam(defaultValue = "20") size: Int,
    ): PagedVocabularyResponseDto {
        val filter = VocabularyFilterRequestDto(
            jlptLevel = jlptLevel,
            category = category,
            contentType = contentType,
            keyword = keyword,
            page = page,
            size = size,
        )
        val result = vocabularyService.filterVocabulary(filter)
        return result
    }

    @PreAuthFilter(hasAnyRole = ["ADMIN", "USER"])
    @PutMapping("/{vocabId}")
    fun updateVocabulary(
        @PathVariable vocabId: UUID,
        @Valid @RequestBody request: CreateVocabularyRequestDto,
    ): VocabularyDto {
        val result = vocabularyService.updateVocabulary(vocabId, request)
        return result
    }

    @PreAuthFilter(hasAnyRole = ["ADMIN", "USER"])
    @DeleteMapping("/{vocabId}")
    fun deleteVocabulary(@PathVariable vocabId: UUID): ResponseDto {
        vocabularyService.deleteVocabulary(vocabId)
        return ResponseDto(
            status = ResponseType.OK,
            message = "Vocabulary deleted successfully",
        )
    }

    @PreAuthFilter(hasAnyRole = ["ADMIN", "USER"])
    @PostMapping("/{vocabId}/save")
    fun saveVocabularyToNotebook(@PathVariable vocabId: UUID): VocabularyDto {
        val result = vocabularyService.saveVocabularyToNotebook(vocabId)
        return result
    }

    @PreAuthFilter(hasAnyRole = ["ADMIN", "USER"])
    @DeleteMapping("/{vocabId}/save")
    fun removeVocabularyFromNotebook(@PathVariable vocabId: UUID): VocabularyDto {
        val result = vocabularyService.removeVocabularyFromNotebook(vocabId)
        return result
    }

    @PreAuthFilter(hasAnyRole = ["ADMIN", "USER"])
    @GetMapping("/saved")
    fun getSavedVocabulary(
        @RequestParam(defaultValue = "0") page: Int,
        @RequestParam(defaultValue = "20") size: Int,
    ): PagedVocabularyResponseDto {
        val filter = VocabularyFilterRequestDto(page = page, size = size)
        val result = vocabularyService.getSavedVocabulary(filter)
        return result
    }

    @GetMapping("/categories")
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

    @GetMapping("/content-types")
    fun getContentTypes(): List<String> {
        val contentTypes = listOf("vocabulary", "grammar", "conversation")
        return contentTypes
    }

    @GetMapping("/jlpt-levels")
    fun getJLPTLevels(): List<JLPTLevel> {
        val levels = JLPTLevel.entries
        return levels
    }
}
