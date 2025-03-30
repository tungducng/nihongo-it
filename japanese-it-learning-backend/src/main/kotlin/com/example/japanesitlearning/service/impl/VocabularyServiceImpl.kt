package com.example.japanesitlearning.service.impl

import com.example.japanesitlearning.dto.vocabulary.CreateVocabularyRequest
import com.example.japanesitlearning.dto.vocabulary.UpdateVocabularyRequest
import com.example.japanesitlearning.dto.vocabulary.VocabularyPageResponse
import com.example.japanesitlearning.dto.vocabulary.VocabularyResponse
import com.example.japanesitlearning.entity.VocabularyEntity
import com.example.japanesitlearning.repository.LessonRepository
import com.example.japanesitlearning.repository.VocabularyRepository
import com.example.japanesitlearning.service.VocabularyService
import org.springframework.data.domain.Page
import org.springframework.data.domain.Pageable
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import java.util.NoSuchElementException
import java.util.UUID

@Service
class VocabularyServiceImpl(
    private val vocabularyRepository: VocabularyRepository,
    private val lessonRepository: LessonRepository
) : VocabularyService {

    @Transactional
    override fun createVocabulary(request: CreateVocabularyRequest): VocabularyResponse {
        val lesson = lessonRepository.findById(request.lessonId)
            .orElseThrow { NoSuchElementException("Lesson not found with ID: ${request.lessonId}") }

        val vocabulary = VocabularyEntity(
            kanji = request.kanji,
            hiragana = request.hiragana,
            meaning = request.meaning,
            exampleSentence = request.exampleSentence,
            lesson = lesson,
            partOfSpeech = request.partOfSpeech,
            itContext = request.itContext,
            audioUrl = request.audioUrl,
            similarWords = request.similarWords,
            mnemonicHint = request.mnemonicHint,
            jlptLevel = request.jlptLevel
        )

        val saved = vocabularyRepository.save(vocabulary)
        return mapToResponse(saved)
    }

    override fun getVocabulary(id: UUID): VocabularyResponse {
        val vocabulary = vocabularyRepository.findById(id)
            .orElseThrow { NoSuchElementException("Vocabulary not found with ID: $id") }
        return mapToResponse(vocabulary)
    }

    override fun getAllVocabularies(pageable: Pageable): VocabularyPageResponse {
        val page = vocabularyRepository.findAll(pageable)
        return createPageResponse(page)
    }

    @Transactional
    override fun updateVocabulary(id: UUID, request: UpdateVocabularyRequest): VocabularyResponse {
        val vocabulary = vocabularyRepository.findById(id)
            .orElseThrow { NoSuchElementException("Vocabulary not found with ID: $id") }

        val lesson = lessonRepository.findById(request.lessonId)
            .orElseThrow { NoSuchElementException("Lesson not found with ID: ${request.lessonId}") }

        val updated = vocabulary.copy(
            kanji = request.kanji,
            hiragana = request.hiragana,
            meaning = request.meaning,
            exampleSentence = request.exampleSentence,
            lesson = lesson,
            partOfSpeech = request.partOfSpeech,
            itContext = request.itContext,
            audioUrl = request.audioUrl,
            similarWords = request.similarWords,
            mnemonicHint = request.mnemonicHint,
            jlptLevel = request.jlptLevel
        )

        val saved = vocabularyRepository.save(updated)
        return mapToResponse(saved)
    }

    @Transactional
    override fun deleteVocabulary(id: UUID) {
        if (!vocabularyRepository.existsById(id)) {
            throw NoSuchElementException("Vocabulary not found with ID: $id")
        }
        vocabularyRepository.deleteById(id)
    }

    override fun searchVocabularies(keyword: String, pageable: Pageable): VocabularyPageResponse {
        val page = vocabularyRepository.searchByKeyword(keyword, pageable)
        return createPageResponse(page)
    }

    override fun getVocabulariesByJlptLevel(level: Int, pageable: Pageable): VocabularyPageResponse {
        val page = vocabularyRepository.findByJlptLevel(level, pageable)
        return createPageResponse(page)
    }

    override fun getItVocabularies(pageable: Pageable): VocabularyPageResponse {
        val page = vocabularyRepository.findByItContextTrue(pageable)
        return createPageResponse(page)
    }

    override fun getVocabulariesByLesson(lessonId: UUID, pageable: Pageable): VocabularyPageResponse {
        val page = vocabularyRepository.findByLessonId(lessonId, pageable)
        return createPageResponse(page)
    }

    private fun mapToResponse(vocabulary: VocabularyEntity): VocabularyResponse {
        return VocabularyResponse(
            id = vocabulary.vocabularyId!!,
            kanji = vocabulary.kanji,
            hiragana = vocabulary.hiragana,
            meaning = vocabulary.meaning,
            exampleSentence = vocabulary.exampleSentence,
            lessonId = vocabulary.lesson.lessonId!!,
            lessonName = vocabulary.lesson.lessonName,
            partOfSpeech = vocabulary.partOfSpeech,
            itContext = vocabulary.itContext,
            audioUrl = vocabulary.audioUrl,
            similarWords = vocabulary.similarWords,
            mnemonicHint = vocabulary.mnemonicHint,
            jlptLevel = vocabulary.jlptLevel
        )
    }

    private fun createPageResponse(page: Page<VocabularyEntity>): VocabularyPageResponse {
        return VocabularyPageResponse(
            content = page.content.map { mapToResponse(it) },
            page = page.number,
            size = page.size,
            totalElements = page.totalElements,
            totalPages = page.totalPages
        )
    }
}