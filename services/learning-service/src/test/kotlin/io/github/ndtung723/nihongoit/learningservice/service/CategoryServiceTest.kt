package io.github.ndtung723.nihongoit.learningservice.service

import io.github.ndtung723.nihongoit.common.exception.BusinessException
import io.github.ndtung723.nihongoit.learningservice.dto.CreateCategoryRequest
import io.github.ndtung723.nihongoit.learningservice.dto.UpdateCategoryRequest
import io.github.ndtung723.nihongoit.learningservice.entity.CategoryEntity
import io.github.ndtung723.nihongoit.learningservice.repository.CategoryRepository
import io.github.ndtung723.nihongoit.learningservice.repository.TopicRepository
import io.github.ndtung723.nihongoit.learningservice.util.UserAuthUtil
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows
import org.mockito.kotlin.any
import org.mockito.kotlin.argThat
import org.mockito.kotlin.mock
import org.mockito.kotlin.never
import org.mockito.kotlin.verify
import org.mockito.kotlin.whenever
import java.util.Optional
import java.util.UUID
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertTrue

class CategoryServiceTest {
    private lateinit var categoryRepository: CategoryRepository
    private lateinit var topicRepository: TopicRepository
    private lateinit var userAuthUtil: UserAuthUtil
    private lateinit var service: CategoryService

    private val userId = UUID.randomUUID()
    private val categoryId = UUID.randomUUID()

    private fun makeCategory(isActive: Boolean = true) =
        CategoryEntity(
            categoryId = categoryId,
            name = "Test Category",
            meaning = "テスト",
            displayOrder = 1,
            isActive = isActive,
        )

    @BeforeEach
    fun setup() {
        categoryRepository = mock()
        topicRepository = mock()
        userAuthUtil = mock()
        service = CategoryService(categoryRepository, topicRepository, userAuthUtil)
    }

    @Nested
    @DisplayName("toggleCategoryStatus()")
    inner class ToggleCategoryStatus {
        @Test
        @DisplayName("active category → flips to inactive")
        fun activeCategory_becomesInactive() {
            val active = makeCategory(isActive = true)
            whenever(categoryRepository.findById(categoryId)).thenReturn(Optional.of(active))
            whenever(categoryRepository.save(any())).thenAnswer { it.arguments[0] }

            val result = service.toggleCategoryStatus(categoryId)

            assertFalse(result.isActive)
            verify(categoryRepository).save(argThat<CategoryEntity> { !isActive })
        }

        @Test
        @DisplayName("inactive category → flips to active")
        fun inactiveCategory_becomesActive() {
            val inactive = makeCategory(isActive = false)
            whenever(categoryRepository.findById(categoryId)).thenReturn(Optional.of(inactive))
            whenever(categoryRepository.save(any())).thenAnswer { it.arguments[0] }

            val result = service.toggleCategoryStatus(categoryId)

            assertTrue(result.isActive)
        }

        @Test
        @DisplayName("idempotent: toggling twice returns to original state")
        fun doubleToggle_returnsToOriginal() {
            val active = makeCategory(isActive = true)
            whenever(categoryRepository.findById(categoryId)).thenReturn(Optional.of(active))
            whenever(categoryRepository.save(any<CategoryEntity>())).thenAnswer { invocation ->
                invocation.arguments[0] as CategoryEntity
            }

            val afterFirst = service.toggleCategoryStatus(categoryId)
            assertFalse(afterFirst.isActive)

            val inactiveEntity = active.copy(isActive = false)
            whenever(categoryRepository.findById(categoryId)).thenReturn(Optional.of(inactiveEntity))

            val afterSecond = service.toggleCategoryStatus(categoryId)
            assertTrue(afterSecond.isActive)
        }

        @Test
        @DisplayName("category not found → throws BusinessException")
        fun notFound_throwsBusinessException() {
            whenever(categoryRepository.findById(categoryId)).thenReturn(Optional.empty())

            assertThrows<BusinessException> {
                service.toggleCategoryStatus(categoryId)
            }
        }
    }

    @Nested
    @DisplayName("createCategory()")
    inner class CreateCategory {
        @Test
        @DisplayName("duplicate name → throws BusinessException, repository.save never called")
        fun duplicateName_throwsBusinessException() {
            val request = CreateCategoryRequest(name = "Existing", meaning = "既存")
            whenever(userAuthUtil.getCurrentUserId()).thenReturn(userId)
            whenever(categoryRepository.existsByName("Existing")).thenReturn(true)

            assertThrows<BusinessException> {
                service.createCategory(request)
            }
            verify(categoryRepository, never()).save(any())
        }

        @Test
        @DisplayName("happy path → saves and returns CategoryDTO with provided name")
        fun happyPath_savesAndReturnsDto() {
            val request = CreateCategoryRequest(name = "New Category", meaning = "新カテゴリ", displayOrder = 5)
            whenever(userAuthUtil.getCurrentUserId()).thenReturn(userId)
            whenever(categoryRepository.existsByName("New Category")).thenReturn(false)
            whenever(categoryRepository.save(any())).thenAnswer { invocation ->
                val entity = invocation.arguments[0] as CategoryEntity
                entity.copy(categoryId = UUID.randomUUID())
            }

            val result = service.createCategory(request)

            assertEquals("New Category", result.name)
            assertEquals("新カテゴリ", result.meaning)
            verify(categoryRepository).save(any())
        }

        @Test
        @DisplayName("not authenticated → throws BusinessException")
        fun notAuthenticated_throwsBusinessException() {
            whenever(userAuthUtil.getCurrentUserId()).thenReturn(null)

            assertThrows<BusinessException> {
                service.createCategory(CreateCategoryRequest(name = "X", meaning = "Y"))
            }
        }
    }

    @Nested
    @DisplayName("getCategoryById()")
    inner class GetCategoryById {
        @Test
        @DisplayName("existing ID → returns DTO")
        fun existingId_returnsDto() {
            val category = makeCategory()
            whenever(categoryRepository.findById(categoryId)).thenReturn(Optional.of(category))

            val result = service.getCategoryById(categoryId)

            assertEquals(categoryId, result.categoryId)
            assertEquals("Test Category", result.name)
        }

        @Test
        @DisplayName("non-existent ID → throws BusinessException")
        fun nonExistentId_throwsBusinessException() {
            whenever(categoryRepository.findById(categoryId)).thenReturn(Optional.empty())

            assertThrows<BusinessException> {
                service.getCategoryById(categoryId)
            }
        }
    }

    @Nested
    @DisplayName("updateCategory()")
    inner class UpdateCategory {
        @Test
        @DisplayName("renaming to existing name → throws BusinessException")
        fun renameToDuplicateName_throwsBusinessException() {
            val existing = makeCategory()
            val request = UpdateCategoryRequest(name = "Taken Name")
            whenever(categoryRepository.findById(categoryId)).thenReturn(Optional.of(existing))
            whenever(categoryRepository.existsByName("Taken Name")).thenReturn(true)

            assertThrows<BusinessException> {
                service.updateCategory(categoryId, request)
            }
        }
    }
}
