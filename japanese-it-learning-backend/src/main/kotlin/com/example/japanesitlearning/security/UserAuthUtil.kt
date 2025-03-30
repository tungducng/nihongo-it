package com.example.japanesitlearning.security

import com.example.japanesitlearning.entity.UserEntity
import jakarta.servlet.http.HttpServletRequest
import org.slf4j.LoggerFactory
import org.springframework.stereotype.Component
import org.springframework.web.context.request.RequestContextHolder
import org.springframework.web.context.request.ServletRequestAttributes
import java.util.UUID

@Component
class UserAuthUtil(
    private val jwtTokenUtil: JwtTokenUtil
) {

    private val logger = LoggerFactory.getLogger(UserAuthUtil::class.java)

    // Lấy token từ header Authorization
    private fun getTokenFromRequest(): String? {
        val request = (RequestContextHolder.getRequestAttributes() as? ServletRequestAttributes)?.request
        val authHeader = request?.getHeader("Authorization")
        return if (authHeader != null && authHeader.startsWith("Bearer ")) {
            authHeader.substring(7) // Bỏ "Bearer "
        } else {
            logger.warn("No valid Authorization header found")
            null
        }
    }

    /**
     * Trích xuất email từ token
     */
    fun getEmailFromToken(token: String): String? {
        return try {
            jwtTokenUtil.extractEmail(token)
        } catch (e: Exception) {
            logger.error("Failed to extract email from token: ${e.message}")
            null
        }
    }

    /**
     * Trích xuất username từ token
     */
    fun getUsernameFromToken(token: String): String? {
        return try {
            jwtTokenUtil.extractUsername(token)
        } catch (e: Exception) {
            logger.error("Failed to extract username from token: ${e.message}")
            null
        }
    }

    /**
     * Trích xuất id từ token
     */
    fun getIdFromToken(token: String): UUID? {
        return try {
            jwtTokenUtil.extractClaim(token) { claims -> claims["userId"] as UUID }
        } catch (e: Exception) {
            logger.error("Failed to extract userId from token: ${e.message}")
            null
        }
    }

    /**
     * Trích xuất roleId từ token
     */
    fun getRoleIdFromToken(token: String): Int? {
        return try {
            jwtTokenUtil.extractRoleId(token)
        } catch (e: Exception) {
            logger.error("Failed to extract roleId from token: ${e.message}")
            null
        }
    }

    /**
     * Tạo một UserEntity từ token (nếu cần toàn bộ thông tin người dùng)
     */
    fun getUserFromToken(token: String): UserEntity? {
        return try {
            val userId = getIdFromToken(token)
            val username = getUsernameFromToken(token)
            val email = getEmailFromToken(token)
            val roleId = getRoleIdFromToken(token)

            if (userId == null || username == null || email == null || roleId == null) {
                logger.warn("Incomplete user data extracted from token: id=$userId, username=$username, email=$email, roleId=$roleId")
                return null
            }

            UserEntity(
                userId,
                username,
                password = "", // Không lưu password trong token, để trống
                email,
                roleId
            )
        } catch (e: Exception) {
            logger.error("Failed to create UserEntity from token: ${e.message}")
            null
        }
    }

    /**
     * Lấy thông tin người dùng từ header Authorization
     */
    fun getCurrentUser(): UserEntity? {
        val token = getTokenFromRequest() ?: return null
        return getUserFromToken(token)
    }

    /**
     * Lấy username từ header Authorization
     */
    fun getCurrentUsername(): String? {
        val token = getTokenFromRequest() ?: return null
        return getUsernameFromToken(token)
    }

    /**
     * Lấy email từ header Authorization
     */
    fun getCurrentEmail(): String? {
        val token = getTokenFromRequest() ?: return null
        return getEmailFromToken(token)
    }

    /**
     * Lấy roleId từ header Authorization
     */
    fun getCurrentRoleId(): Int? {
        val token = getTokenFromRequest() ?: return null
        return getRoleIdFromToken(token)
    }
}