package com.example.nihongoit.entity

import jakarta.persistence.*
import java.time.LocalDateTime
import java.util.*

@Entity
@Table(name = "users")
data class UserEntity(
    @Id
    @GeneratedValue(generator = "UUID")
    @Column(name = "user_id", updatable = false, nullable = false)
    val userId: UUID? = null,

    @Column(name = "email", nullable = false, unique = true, length = 50)
    val email: String,

    @Column(name = "password", nullable = false)
    val password: String,

    @Column(name = "full_name", nullable = false, length = 100)
    val fullName: String,

    @Column(name = "profile_picture")
    val profilePicture: String?,

    @Enumerated(EnumType.STRING)
    @Column(name = "current_level")
    val currentLevel: JLPTLevel?,

    @Enumerated(EnumType.STRING)
    @Column(name = "jlpt_goal")
    val jlptGoal: JLPTLevel?,

    @Column(name = "is_active")
    val isActive: Boolean = true,

    @Column(name = "last_login")
    val lastLogin: LocalDateTime?,

    @Column(name = "streak_count")
    val streakCount: Int = 0,

    @Column(name = "points")
    val points: Int = 0,

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "role_id", nullable = false)
    var role: RoleEntity,

    @Column(name = "created_at")
    val createdAt: LocalDateTime = LocalDateTime.now(),

    @Column(name = "updated_at")
    val updatedAt: LocalDateTime = LocalDateTime.now(),
)
