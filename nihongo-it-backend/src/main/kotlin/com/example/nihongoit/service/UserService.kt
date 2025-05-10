package com.example.nihongoit.service

import com.example.nihongoit.controller.UpdatePreferencesRequest
import com.example.nihongoit.entity.UserEntity
import com.example.nihongoit.exception.BusinessException
import com.example.nihongoit.repository.UserRepository
import com.example.nihongoit.repository.ReviewLogRepository
import jakarta.persistence.EntityNotFoundException
import org.slf4j.LoggerFactory
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import java.time.LocalDateTime
import java.time.LocalTime
import java.time.format.DateTimeFormatter
import java.util.UUID

@Service
class UserService @Autowired constructor(
    private val userRepository: UserRepository,
    private val reviewLogRepository: ReviewLogRepository
) {
    private val logger = LoggerFactory.getLogger(UserService::class.java)
    
    /**
     * Get user profile information
     */
    fun getUserProfile(userId: UUID): Map<String, Any> {
        val user = getUserById(userId)
        
        return mapOf(
            "userId" to (user.userId ?: ""),
            "email" to user.email,
            "fullName" to user.fullName,
            "profilePicture" to (user.profilePicture ?: ""),
            "currentLevel" to (user.currentLevel?.name ?: ""),
            "jlptGoal" to (user.jlptGoal?.name ?: ""),
            "streakCount" to user.streakCount,
            "points" to user.points,
            "lastLogin" to (user.lastLogin?.toString() ?: ""),
            "isEmailVerified" to user.isEmailVerified,
            "createdAt" to user.createdAt.toString()
        )
    }
    
    /**
     * Update notification preferences for a user
     */
    @Transactional
    fun updateNotificationPreferences(userId: UUID, request: UpdatePreferencesRequest) {
        logger.info("Updating notification preferences for user $userId")
        
        val user = getUserById(userId)
        val timeFormatter = DateTimeFormatter.ofPattern("HH:mm")
        
        // Create a copy of the user with updated preferences
        val updatedUser = user.copy(
            notificationPreferences = request.notificationPreferences ?: user.notificationPreferences,
            reminderEnabled = request.reminderEnabled ?: user.reminderEnabled,
            reminderTime = if (request.reminderTime != null) {
                LocalTime.parse(request.reminderTime, timeFormatter)
            } else {
                user.reminderTime
            },
            minCardThreshold = request.minCardThreshold ?: user.minCardThreshold,
            updatedAt = LocalDateTime.now()
        ).let { updatedUser ->
            // Handle leech notifications by adding/removing "leech" from notificationPreferences
            if (request.leechNotificationsEnabled != null) {
                val preferences = updatedUser.notificationPreferences.split(",").toMutableList()
                
                if (request.leechNotificationsEnabled && !preferences.contains("leech")) {
                    preferences.add("leech")
                } else if (!request.leechNotificationsEnabled) {
                    preferences.remove("leech")
                }
                
                updatedUser.copy(notificationPreferences = preferences.joinToString(","))
            } else {
                updatedUser
            }
        }
        
        userRepository.save(updatedUser)
        logger.info("Notification preferences updated for user $userId")
    }
    
    /**
     * Get notification preferences for a user
     */
    fun getNotificationPreferences(userId: UUID): Map<String, Any> {
        val user = getUserById(userId)
        
        val notificationPrefs = user.notificationPreferences.split(",").map { it.trim() }
        
        return mapOf(
            "notificationPreferences" to user.notificationPreferences,
            "reminderEnabled" to user.reminderEnabled,
            "reminderTime" to (user.reminderTime?.format(DateTimeFormatter.ofPattern("HH:mm")) ?: "20:00"),
            "minCardThreshold" to (user.minCardThreshold ?: 5)
        )
    }
    
    /**
     * Get user by IDi
     */
    fun getUserById(userId: UUID): UserEntity {
        return userRepository.findById(userId)
            .orElseThrow { BusinessException("User not found with ID: $userId") }
    }

    // Dashboard statistics methods
    
    /**
     * Get total number of users in the system
     */
    fun getUserCount(): Int {
        return userRepository.count().toInt()
    }
    
    /**
     * Get number of new users registered since the given date
     */
    fun getNewUserCount(sinceDate: LocalDateTime): Int {
        return userRepository.countByCreatedAtAfter(sinceDate).toInt()
    }
    
    /**
     * Get number of active users who logged in since the given date
     */
    fun getActiveUserCount(sinceDate: LocalDateTime): Int {
        return userRepository.countByLastLoginAfter(sinceDate).toInt()
    }
    
    /**
     * Get recent user activities for the dashboard
     */
    fun getRecentUserActivities(limit: Int): List<Map<String, Any>> {
        // This is a placeholder implementation. 
        // In a real application, you would retrieve this data from an activity log table
        val recentUsersWithActivity = userRepository.findTop10ByOrderByLastLoginDesc()
        
        val activities = recentUsersWithActivity.mapIndexedNotNull { index, user ->
            if (index < limit && user.lastLogin != null) {
                val action = when {
                    user.lastLogin!!.isAfter(LocalDateTime.now().minusHours(1)) -> "Đã đăng nhập"
                    user.createdAt.isAfter(LocalDateTime.now().minusDays(1)) -> "Đã tạo tài khoản mới"
                    else -> "Đã truy cập hệ thống"
                }
                
                mapOf(
                    "user" to user.email,
                    "action" to action,
                    "timestamp" to user.lastLogin
                )
            } else null
        }
        
        // Ensure we always return a list, even if empty
        return activities as List<Map<String, Any>>
    }

    /**
     * Get top performing users by retention rate
     */
    fun getTopPerformingUsers(limit: Int): List<UserEntity> {
        // Find users with the highest retention rates
        val thirtyDaysAgo = LocalDateTime.now().minusDays(30)
        
        // This is a simplified approach - in a real implementation, you would likely 
        // want to query the database directly for this information
        val allUsers = userRepository.findAll()
        
        val userStats = allUsers.map { user ->
            // Get review data for each user
            val reviews = reviewLogRepository.findByUserIdAndReviewTimestampAfterOrderByReviewTimestampDesc(
                user.userId!!, thirtyDaysAgo
            )
            
            // Skip users with no reviews
            if (reviews.isEmpty()) {
                return@map Pair(user, 0.0)
            }
            
            // Calculate retention rate
            val correctReviews = reviews.count { it.rating >= 3 }
            val retentionRate = (correctReviews.toDouble() / reviews.size) * 100
            
            Pair(user, retentionRate)
        }
        
        // Filter out users with no reviews, then sort by retention rate and limit
        return userStats
            .filter { it.second > 0 }
            .sortedByDescending { it.second }
            .take(limit)
            .map { it.first }
    }
    
    /**
     * Get most active users by number of reviews
     */
    fun getMostActiveUsers(limit: Int): List<UserEntity> {
        val thirtyDaysAgo = LocalDateTime.now().minusDays(30)
        
        // This is a simplified approach - in a real implementation, you would likely 
        // want to query the database directly for this information
        val allUsers = userRepository.findAll()
        
        val userStats = allUsers.map { user ->
            // Count reviews for each user
            val reviewCount = reviewLogRepository.countByUserIdAndReviewTimestampAfter(
                user.userId!!, thirtyDaysAgo
            )
            
            Pair(user, reviewCount)
        }
        
        // Sort by review count and limit
        return userStats
            .filter { it.second > 0 }
            .sortedByDescending { it.second }
            .take(limit)
            .map { it.first }
    }
    
    /**
     * Get count of users by current level
     */
    fun getUserCountByCurrentLevel(): Map<out Any, Int> {
        val allUsers = userRepository.findAll()
        
        // Group users by current level
        return allUsers
            .groupBy { it.currentLevel ?: "Not specified" }
            .mapValues { it.value.size }
    }
    
    /**
     * Get count of users by JLPT goal
     */
    fun getUserCountByJlptGoal(): Map<out Any, Int> {
        val allUsers = userRepository.findAll()
        
        // Group users by JLPT goal
        return allUsers
            .groupBy { it.jlptGoal ?: "Not specified" }
            .mapValues { it.value.size }
    }

    /**
     * Get all users with pagination
     */
    fun getAllUsers(pageable: org.springframework.data.domain.Pageable): org.springframework.data.domain.Page<UserEntity> {
        // Since lastActive is not a field in UserEntity, we need special handling
        val sortBy = pageable.sort.map { order -> order.property }.firstOrNull() ?: "userId"
        
        // For non-standard sort fields, we need to fetch all and sort in-memory
        if (sortBy == "lastActive") {
            val allUsers = userRepository.findAll()
            
            // Get review dates for all users
            val userLastActiveDates = allUsers.associate { user ->
                val lastReview = reviewLogRepository.findTopByUserIdOrderByReviewTimestampDesc(user.userId!!)
                val lastActive = lastReview?.reviewTimestamp ?: user.updatedAt ?: user.lastLogin ?: user.createdAt
                user to lastActive
            }
            
            // Sort based on the lastActive dates
            val direction = pageable.sort.getOrderFor(sortBy)?.direction ?: org.springframework.data.domain.Sort.Direction.DESC
            val sortedUsers = if (direction == org.springframework.data.domain.Sort.Direction.ASC) {
                allUsers.sortedBy { userLastActiveDates[it] }
            } else {
                allUsers.sortedByDescending { userLastActiveDates[it] }
            }
            
            // Apply pagination
            val start = pageable.pageNumber * pageable.pageSize
            val end = (start + pageable.pageSize).coerceAtMost(sortedUsers.size)
            val pagedContent = if (start < sortedUsers.size) sortedUsers.subList(start, end) else emptyList()
            
            // Create a custom Page implementation
            return org.springframework.data.domain.PageImpl(
                pagedContent,
                pageable,
                sortedUsers.size.toLong()
            )
        }
        
        // For standard fields, use repository's built-in pagination
        return userRepository.findAll(pageable)
    }
} 