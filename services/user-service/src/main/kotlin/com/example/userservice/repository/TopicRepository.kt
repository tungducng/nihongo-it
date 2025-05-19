package com.example.userservice.repository

import com.example.userservice.entity.CategoryEntity
import com.example.userservice.entity.TopicEntity
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.stereotype.Repository
import java.util.UUID

@Repository
interface TopicRepository : JpaRepository<TopicEntity, UUID> {
    fun findByCategoryOrderByDisplayOrderAsc(category: CategoryEntity): List<TopicEntity>
    
    fun findByCategoryAndNameContainingIgnoreCase(category: CategoryEntity, name: String): List<TopicEntity>
    
    fun findByCategoryAndNameContainingIgnoreCaseOrCategoryAndMeaningContainingIgnoreCase(
        category1: CategoryEntity, name: String, 
        category2: CategoryEntity, meaning: String
    ): List<TopicEntity>
    
    fun findByCategoryCategoryId(categoryId: UUID): List<TopicEntity>
    
    fun findByName(name: String): List<TopicEntity>
    
    fun existsByNameAndCategory(name: String, category: CategoryEntity): Boolean
} 