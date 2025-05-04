package com.example.nihongoit.repository

import com.example.nihongoit.entity.UserEntity
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.stereotype.Repository
import java.util.UUID
import java.util.Optional

@Repository
interface UserRepository : JpaRepository<UserEntity, UUID> {
    fun existsByEmail(email: String): Boolean
    fun findByEmail(email: String): UserEntity?
    fun findByResetPasswordToken(token: String): UserEntity?
    fun findByVerificationToken(token: String): UserEntity?
    
    // Find active users with reminders enabled
    fun findByIsActiveAndReminderEnabled(isActive: Boolean, reminderEnabled: Boolean): List<UserEntity>
} 
