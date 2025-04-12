package com.example.nihongoit.repository

import com.example.nihongoit.entity.UserEntity
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.stereotype.Repository
import java.util.UUID

@Repository
interface UserRepository : JpaRepository<UserEntity, UUID> {
    fun existsByEmail(email: String): Boolean
    fun findByEmail(email: String): UserEntity?
} 
