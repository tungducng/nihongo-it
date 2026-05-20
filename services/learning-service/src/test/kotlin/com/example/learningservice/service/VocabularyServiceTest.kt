package com.example.learningservice.service

import com.example.common.exception.BusinessException
import com.example.learningservice.dto.VocabularyFilterRequestDto
import com.example.learningservice.entity.CategoryEntity
import com.example.learningservice.entity.JlptLevel
import com.example.learningservice.entity.TopicEntity
import com.example.learningservice.entity.VocabularyEntity
import com.example.learningservice.repository.FlashcardRepository
import com.example.learningservice.repository.SavedVocabularyRepository
import com.example.learningservice.repository.TopicRepository
import com.example.learningservice.repository.VocabularyRepository
import com.example.learningservice.util.UserAuthUtil
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows
import org.mockito.kotlin.any
import org.mockito.kotlin.mock
import org.mockito.kotlin.never
import org.mockito.kotlin.verify
import org.mockito.kotlin.whenever
import org.springframework.data.domain.PageImpl
import org.springframework.data.domain.PageRequest
import java.time.Instant
import java.util.Optional
import java.util.UUID
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class VocabularyServiceTest {
    private lateinit var vocabularyRepository: VocabularyRepository
    private lateinit var topicRepository: TopicRepository
    private lateinit var userAuthUtil: UserAuthUtil
    private lateinit var flashcardCrudService: FlashcardCrudService
    private lateinit var flashcardRepository: FlashcardRepository
    private lateinit var savedVocabularyRepository: SavedVocabularyRepository
    private lateinit var service: VocabularyService

    private val userId = UUID.randomUUID()
    private val vocabId = UUID.randomUUID()

    private fun makeTopic(): TopicEntity {
        val category =
            CategoryEntity(
                categoryId = UUID.randomUUID(),
                name = "Test Category",
                meaning = "テスト",
            )
        return TopicEntity(
            topicId = UUID.randomUUID(),
            name = "Test Topic",
            meaning = "テスト",
            category = category,
        )
    }

    private fun makeVocabulary(): VocabularyEntity =
        VocabularyEntity(
            vocabId = vocabId,
            term = "テスト",
            meaning = "test",
            pronunciation = "てすと",
            example = null,
            exampleMeaning = null,
            audioPath = null,
            jlptLevel = JlptLevel.N4,
            topic = makeTopic(),
            createdAt = Instant.now(),
        )

    @BeforeEach
    fun setup() {
        vocabularyRepository = mock()
        topicRepository = mock()
        userAuthUtil = mock()
        flashcardCrudService = mock()
        flashcardRepository = mock()
        savedVocabularyRepository = mock()
        service =
            VocabularyService(
                vocabularyRepository,
                topicRepository,
                userAuthUtil,
                flashcardCrudService,
                flashcardRepository,
                savedVocabularyRepository,
            )
    }

    @Nested
    @DisplayName("saveVocabularyToNotebook()")
    inner class SaveVocabularyToNotebook {
        @Test
        @DisplayName("happy path → inserts row in saved_vocabulary, isSaved=true")
        fun happyPath_addsSavedUser() {
            val vocab = makeVocabulary()
            whenever(userAuthUtil.getCurrentUserId()).thenReturn(userId)
            whenever(vocabularyRepository.findById(vocabId)).thenReturn(Optional.of(vocab))
            whenever(savedVocabularyRepository.existsByVocabIdAndUserId(vocabId, userId)).thenReturn(false)

            val result = service.saveVocabularyToNotebook(vocabId)

            assertTrue(result.isSaved)
            verify(savedVocabularyRepository).save(any())
        }

        @Test
        @DisplayName("auto-creates flashcard (silently ignores if flashcard already exists)")
        fun autoCreatesFlashcard() {
            val vocab = makeVocabulary()
            whenever(userAuthUtil.getCurrentUserId()).thenReturn(userId)
            whenever(vocabularyRepository.findById(vocabId)).thenReturn(Optional.of(vocab))
            whenever(savedVocabularyRepository.existsByVocabIdAndUserId(vocabId, userId)).thenReturn(false)
            whenever(flashcardCrudService.createFlashcardFromVocabulary(vocabId))
                .thenThrow(BusinessException("Flashcard already exists"))

            val result = service.saveVocabularyToNotebook(vocabId)

            assertTrue(result.isSaved)
        }

        @Test
        @DisplayName("vocabulary not found → throws")
        fun vocabNotFound_throwsBusinessException() {
            whenever(userAuthUtil.getCurrentUserId()).thenReturn(userId)
            whenever(vocabularyRepository.findById(vocabId)).thenReturn(Optional.empty())

            assertThrows<Exception> {
                service.saveVocabularyToNotebook(vocabId)
            }
        }

        @Test
        @DisplayName("not authenticated → throws BusinessException")
        fun notAuthenticated_throwsBusinessException() {
            whenever(userAuthUtil.getCurrentUserId()).thenReturn(null)

            assertThrows<BusinessException> {
                service.saveVocabularyToNotebook(vocabId)
            }
            verify(savedVocabularyRepository, never()).save(any())
        }
    }

    @Nested
    @DisplayName("removeVocabularyFromNotebook()")
    inner class RemoveVocabularyFromNotebook {
        @Test
        @DisplayName("happy path → deletes saved_vocabulary row, isSaved=false")
        fun happyPath_removesSavedUser() {
            val vocab = makeVocabulary()
            whenever(userAuthUtil.getCurrentUserId()).thenReturn(userId)
            whenever(vocabularyRepository.findById(vocabId)).thenReturn(Optional.of(vocab))
            whenever(flashcardRepository.findByUserIdAndVocabulary_VocabId(userId, vocabId))
                .thenReturn(emptyList())

            val result = service.removeVocabularyFromNotebook(vocabId)

            assertEquals(false, result.isSaved)
            verify(savedVocabularyRepository).deleteByVocabAndUser(vocabId, userId)
        }

        @Test
        @DisplayName("not authenticated → throws BusinessException")
        fun notAuthenticated_throwsBusinessException() {
            whenever(userAuthUtil.getCurrentUserId()).thenReturn(null)

            assertThrows<BusinessException> {
                service.removeVocabularyFromNotebook(vocabId)
            }
        }
    }

    @Nested
    @DisplayName("filterVocabulary()")
    inner class FilterVocabulary {
        @Test
        @DisplayName("keyword filter → calls searchVocabulary")
        fun withKeyword_callsSearchVocabulary() {
            val filter = VocabularyFilterRequestDto(keyword = "テスト", page = 0, size = 10)
            val emptyPage = PageImpl<VocabularyEntity>(emptyList(), PageRequest.of(0, 10), 0)
            whenever(userAuthUtil.getCurrentUserId()).thenReturn(null)
            whenever(vocabularyRepository.searchVocabulary(any(), any())).thenReturn(emptyPage)

            service.filterVocabulary(filter)

            verify(vocabularyRepository).searchVocabulary(any(), any())
        }

        @Test
        @DisplayName("no filters → calls findAll(pageable)")
        fun noFilters_callsFindAll() {
            val filter = VocabularyFilterRequestDto(page = 0, size = 20)
            val emptyPage = PageImpl<VocabularyEntity>(emptyList(), PageRequest.of(0, 20), 0)
            whenever(userAuthUtil.getCurrentUserId()).thenReturn(null)
            whenever(vocabularyRepository.findAll(any<org.springframework.data.domain.Pageable>())).thenReturn(emptyPage)

            service.filterVocabulary(filter)

            verify(vocabularyRepository).findAll(any<org.springframework.data.domain.Pageable>())
        }
    }
}
