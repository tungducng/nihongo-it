package com.example.userservice.repository

import com.example.userservice.entity.UserEntity
import org.springframework.data.domain.Page
import org.springframework.data.domain.Pageable
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.stereotype.Repository
import java.time.LocalDateTime
import java.util.UUID

@Repository
interface UserRepository : JpaRepository<UserEntity, UUID> {
    fun existsByEmail(email: String): Boolean
    fun findByEmail(email: String): UserEntity?
    fun findByResetPasswordToken(token: String): UserEntity?
    fun findByVerificationToken(token: String): UserEntity?
    
    // Find active users with reminders enabled
    fun findByIsActiveAndReminderEnabled(isActive: Boolean, reminderEnabled: Boolean): List<UserEntity>
    
    // Count users by role and active status (for admin management)
    fun countByRoleRoleIdAndIsActive(roleId: Int, isActive: Boolean): Long
    
    // Search users by email or full name (for admin management)
    fun findByEmailContainingIgnoreCaseOrFullNameContainingIgnoreCase(
        email: String, 
        fullName: String, 
        pageable: Pageable
    ): Page<UserEntity>
    
    // Find users by activity status (for admin management)
    fun findByIsActive(isActive: Boolean, pageable: Pageable): Page<UserEntity>
    
    // Find users by role (for admin management)
    fun findByRoleRoleId(roleId: Int, pageable: Pageable): Page<UserEntity>
    
    // Statistics methods for dashboard
    fun countByCreatedAtAfter(sinceDate: LocalDateTime): Long
    fun countByLastLoginAfter(sinceDate: LocalDateTime): Long
    fun findTop10ByOrderByLastLoginDesc(): List<UserEntity>
    fun findByIsActiveTrue(): List<UserEntity>
    fun findByIsActiveTrueAndStreakCountGreaterThan(streakCount: Int): List<UserEntity>
} 
