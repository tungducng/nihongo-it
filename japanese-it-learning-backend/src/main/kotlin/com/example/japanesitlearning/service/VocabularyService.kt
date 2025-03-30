package com.example.japanesitlearning.service

import com.example.japanesitlearning.dto.vocabulary.CreateVocabularyRequest
import com.example.japanesitlearning.dto.vocabulary.UpdateVocabularyRequest
import com.example.japanesitlearning.dto.vocabulary.VocabularyPageResponse
import com.example.japanesitlearning.dto.vocabulary.VocabularyResponse
import org.springframework.data.domain.Pageable
import java.util.UUID

interface VocabularyService {
    fun createVocabulary(request: CreateVocabularyRequest): VocabularyResponse
    fun getVocabulary(id: UUID): VocabularyResponse
    fun getAllVocabularies(pageable: Pageable): VocabularyPageResponse
    fun updateVocabulary(id: UUID, request: UpdateVocabularyRequest): VocabularyResponse
    fun deleteVocabulary(id: UUID)
    fun searchVocabularies(keyword: String, pageable: Pageable): VocabularyPageResponse
    fun getVocabulariesByJlptLevel(level: Int, pageable: Pageable): VocabularyPageResponse
    fun getItVocabularies(pageable: Pageable): VocabularyPageResponse
    fun getVocabulariesByLesson(lessonId: UUID, pageable: Pageable): VocabularyPageResponse
}