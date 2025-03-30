package com.example.japanesitlearning.repository

import com.example.japanesitlearning.entity.LessonEntity
import org.springframework.data.domain.Page
import org.springframework.data.domain.Pageable
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.stereotype.Repository
import java.util.UUID

@Repository
interface LessonRepository : JpaRepository<LessonEntity, UUID> {
    
    fun findByLessonNameContainingIgnoreCase(lessonName: String, pageable: Pageable): Page<LessonEntity>
    
    fun findByLevel(level: Int, pageable: Pageable): Page<LessonEntity>
    
    fun findByOrderByOrderIndexAsc(): List<LessonEntity>
    
    fun findByLevelOrderByOrderIndexAsc(level: Int): List<LessonEntity>
}