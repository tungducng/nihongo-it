package io.github.ndtung723.nihongoit.userservice.repository

import io.github.ndtung723.nihongoit.userservice.entity.UserEntity
import org.springframework.data.domain.Page
import org.springframework.data.domain.Pageable
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.jpa.repository.Query
import org.springframework.stereotype.Repository
import java.time.LocalDateTime
import java.util.UUID

@Repository
interface UserRepository : JpaRepository<UserEntity, UUID> {
    fun existsByEmail(email: String): Boolean

    fun findByEmail(email: String): UserEntity?

    fun findByResetPasswordToken(token: String): UserEntity?

    fun findByVerificationToken(token: String): UserEntity?

    fun countByRoleRoleIdAndIsActive(
        roleId: Int,
        isActive: Boolean,
    ): Long

    fun findByEmailContainingIgnoreCaseOrFullNameContainingIgnoreCase(
        email: String,
        fullName: String,
        pageable: Pageable,
    ): Page<UserEntity>

    // Admin stats — surface the simple aggregates that the admin dashboard reads.
    fun countByCreatedAtAfter(createdAt: LocalDateTime): Long

    fun countByLastLoginAfter(lastLogin: LocalDateTime): Long

    fun findTop10ByOrderByLastLoginDesc(): List<UserEntity>

    @Query("SELECT u.currentLevel, COUNT(u) FROM UserEntity u GROUP BY u.currentLevel")
    fun countGroupByCurrentLevel(): List<Array<Any?>>

    @Query("SELECT u.jlptGoal, COUNT(u) FROM UserEntity u GROUP BY u.jlptGoal")
    fun countGroupByJlptGoal(): List<Array<Any?>>
}
