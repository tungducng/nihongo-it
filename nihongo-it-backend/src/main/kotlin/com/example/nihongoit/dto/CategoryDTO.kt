package com.example.nihongoit.dto

import com.example.nihongoit.entity.CategoryEntity
import com.fasterxml.jackson.annotation.JsonInclude
import java.time.LocalDateTime
import java.util.UUID

@JsonInclude(JsonInclude.Include.NON_NULL)
data class CategoryDTO(
    val categoryId: UUID?,
    val name: String,
    val meaning: String? = null,
    val displayOrder: Int = 0,
    val topicCount: Int? = null,
    val createdAt: LocalDateTime? = null,
    val updatedAt: LocalDateTime? = null
)

// Extension function to convert entity to DTO
fun CategoryEntity.toDTO(topicCount: Int? = null): CategoryDTO {
    return CategoryDTO(
        categoryId = this.categoryId,
        name = this.name,
        meaning = this.meaning,
        displayOrder = this.displayOrder,
        topicCount = topicCount ?: this.topics.size,
        createdAt = this.createdAt,
        updatedAt = this.updatedAt
    )
}

// Data class for creating a new category
data class CreateCategoryRequest(
    val name: String,
    val meaning: String,
    val description: String? = null,
    val displayOrder: Int = 0
)

// Data class for updating an existing category
data class UpdateCategoryRequest(
    val name: String? = null,
    val meaning: String? = null,
    val description: String? = null,
    val displayOrder: Int? = null
) 