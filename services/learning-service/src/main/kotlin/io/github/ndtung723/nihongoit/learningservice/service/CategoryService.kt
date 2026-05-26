package io.github.ndtung723.nihongoit.learningservice.service

import io.github.ndtung723.nihongoit.common.exception.BusinessException
import io.github.ndtung723.nihongoit.learningservice.dto.CategoryDTO
import io.github.ndtung723.nihongoit.learningservice.dto.CreateCategoryRequest
import io.github.ndtung723.nihongoit.learningservice.dto.TopicDTO
import io.github.ndtung723.nihongoit.learningservice.dto.UpdateCategoryRequest
import io.github.ndtung723.nihongoit.learningservice.dto.toDTO
import io.github.ndtung723.nihongoit.learningservice.entity.CategoryEntity
import io.github.ndtung723.nihongoit.learningservice.repository.CategoryRepository
import io.github.ndtung723.nihongoit.learningservice.repository.TopicRepository
import io.github.ndtung723.nihongoit.learningservice.util.UserAuthUtil
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import java.util.UUID

@Service
class CategoryService(
    private val categoryRepository: CategoryRepository,
    private val topicRepository: TopicRepository,
    private val userAuthUtil: UserAuthUtil,
) {
    @Transactional(readOnly = true)
    fun getAllCategories(): List<CategoryDTO> = categoryRepository.findAllByOrderByDisplayOrderAsc().map { it.toDTO() }

    @Transactional(readOnly = true)
    fun getCategoryById(categoryId: UUID): CategoryDTO {
        val category =
            categoryRepository
                .findById(categoryId)
                .orElseThrow { BusinessException("Category not found with ID: $categoryId") }

        return category.toDTO()
    }

    @Transactional
    fun createCategory(request: CreateCategoryRequest): CategoryDTO {
        userAuthUtil.getCurrentUserId()
            ?: throw BusinessException("User not authenticated")

        if (categoryRepository.existsByName(request.name)) {
            throw BusinessException("A category with the name '${request.name}' already exists")
        }

        val category =
            CategoryEntity(
                name = request.name,
                meaning = request.meaning,
                displayOrder = request.displayOrder ?: 0,
                isActive = request.isActive ?: true,
            )

        val savedCategory = categoryRepository.save(category)
        return savedCategory.toDTO()
    }

    @Transactional
    fun updateCategory(
        categoryId: UUID,
        request: UpdateCategoryRequest,
    ): CategoryDTO {
        val category =
            categoryRepository
                .findById(categoryId)
                .orElseThrow { BusinessException("Category not found with ID: $categoryId") }

        // Check if another category with the new name already exists (if name is being updated)
        if (request.name != null &&
            request.name != category.name &&
            categoryRepository.existsByName(request.name)
        ) {
            throw BusinessException("A category with the name '${request.name}' already exists")
        }

        val updatedCategory =
            category.copy(
                name = request.name ?: category.name,
                meaning = request.meaning ?: category.meaning,
                displayOrder = request.displayOrder ?: category.displayOrder,
                isActive = request.isActive ?: category.isActive,
            )

        val savedCategory = categoryRepository.save(updatedCategory)
        return savedCategory.toDTO()
    }

    @Transactional
    fun toggleCategoryStatus(categoryId: UUID): CategoryDTO {
        val category =
            categoryRepository
                .findById(categoryId)
                .orElseThrow { BusinessException("Category not found with ID: $categoryId") }

        val updatedCategory =
            category.copy(
                isActive = !category.isActive,
            )

        val savedCategory = categoryRepository.save(updatedCategory)
        return savedCategory.toDTO()
    }

    @Transactional
    fun deleteCategory(categoryId: UUID) {
        if (!categoryRepository.existsById(categoryId)) {
            throw BusinessException("Category not found with ID: $categoryId")
        }

        // Note: Due to cascade.ALL and orphanRemoval=true on the topics relationship,
        // deleting a category will also delete all associated topics and vocabularies
        categoryRepository.deleteById(categoryId)
    }

    @Transactional(readOnly = true)
    fun searchCategories(query: String): List<CategoryDTO> = categoryRepository.findByNameContainingIgnoreCase(query).map { it.toDTO() }

    @Transactional(readOnly = true)
    fun searchCategoriesWithMeaning(
        nameQuery: String?,
        meaningQuery: String?,
    ): List<CategoryDTO> =
        when {
            // If both name and meaning are provided
            !nameQuery.isNullOrBlank() && !meaningQuery.isNullOrBlank() -> {
                categoryRepository
                    .findByNameContainingIgnoreCaseOrMeaningContainingIgnoreCase(
                        nameQuery,
                        meaningQuery,
                    ).map { it.toDTO() }
            }
            // If only name is provided
            !nameQuery.isNullOrBlank() -> {
                categoryRepository.findByNameContainingIgnoreCase(nameQuery).map { it.toDTO() }
            }
            // If only meaning is provided
            !meaningQuery.isNullOrBlank() -> {
                categoryRepository.findByMeaningContainingIgnoreCase(meaningQuery).map { it.toDTO() }
            }
            // If neither is provided, return all categories
            else -> {
                getAllCategories()
            }
        }

    @Transactional(readOnly = true)
    fun getTopicsForCategory(categoryId: UUID): List<TopicDTO> {
        val category =
            categoryRepository
                .findById(categoryId)
                .orElseThrow { BusinessException("Category not found with ID: $categoryId") }

        val topics = topicRepository.findByCategoryOrderByDisplayOrderAsc(category)
        return topics.map { it.toDTO() }
    }

    @Transactional(readOnly = true)
    fun getAllTopics(): List<TopicDTO> {
        // Get all topics sorted by display order
        val topics = topicRepository.findAll().sortedBy { it.displayOrder }
        return topics.map { it.toDTO() }
    }

    /**
     * Get total count of categories
     */
    fun getCategoryCount(): Int = categoryRepository.count().toInt()
}
