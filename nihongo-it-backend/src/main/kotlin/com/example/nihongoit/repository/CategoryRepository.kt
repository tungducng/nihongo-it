package com.example.nihongoit.repository

import com.example.nihongoit.entity.CategoryEntity
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.stereotype.Repository
import java.util.UUID

@Repository
interface CategoryRepository : JpaRepository<CategoryEntity, UUID> {
    fun findByNameContainingIgnoreCase(name: String): List<CategoryEntity>
    
    fun findAllByOrderByDisplayOrderAsc(): List<CategoryEntity>
    
    fun existsByName(name: String): Boolean
} 