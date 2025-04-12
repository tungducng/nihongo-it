package com.example.nihongoit.security

import com.example.nihongoit.entity.JLPTLevel
import com.example.nihongoit.entity.UserEntity
import io.jsonwebtoken.Claims
import io.jsonwebtoken.Jwts
import io.jsonwebtoken.SignatureAlgorithm
import io.jsonwebtoken.security.Keys
import org.slf4j.LoggerFactory
import org.springframework.beans.factory.annotation.Value
import org.springframework.security.core.userdetails.UserDetails
import org.springframework.stereotype.Service
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter
import java.util.Date
import java.util.UUID
import java.util.function.Function
import javax.crypto.SecretKey

@Service
class JwtTokenUtil {

    @Value("\${jwt.secret}")
    private lateinit var secret: String

    @Value("\${jwt.expiration}")
    private val jwtExpiration: Long = 86400000 // 24 hours by default

    private val secretKey: SecretKey by lazy {
        Keys.hmacShaKeyFor(secret.toByteArray())
    }

    private val logger = LoggerFactory.getLogger(JwtTokenUtil::class.java)

    private val dateTimeFormatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm")

    fun generateToken(user: UserEntity): String {
        val claims: Map<String, Any> = mapOf(
            "userId" to (user.userId?.toString() ?: ""),
            "role" to (user.role?.roleId ?: 2), // Default to ROLE_USER
            "email" to user.email,
            "fullName" to (user.fullName ?: ""),
            "profilePicture" to (user.profilePicture ?: ""),
            "currentLevel" to (user.currentLevel?.name ?: ""),
            "jlptGoal" to (user.jlptGoal?.name ?: ""),
            "lastLogin" to (user.lastLogin?.format(dateTimeFormatter) ?: LocalDateTime.now().format(dateTimeFormatter)),
        )
        return createToken(claims, user.email)
    }

    private fun createToken(claims: Map<String, Any>, subject: String): String {
        val now = Date()
        val expirationDate = Date(now.time + jwtExpiration)

        return Jwts.builder()
            .setClaims(claims)
            .setSubject(subject)
            .setIssuedAt(now)
            .setExpiration(expirationDate)
            .signWith(secretKey, SignatureAlgorithm.HS512)
            .compact()
    }

    fun validateToken(token: String, userDetails: UserDetails): Boolean {
        val email = extractEmail(token)
        return email == userDetails.username && !isTokenExpired(token)
    }

    fun extractEmail(token: String): String {
        return extractClaim(token) { claims -> claims["email"] as String }
    }

    fun extractRoleId(jwt: String): Int {
        return extractClaim(jwt) { claims -> claims["role"] as Int }
    }

    fun extractExpiration(token: String): Date {
        return extractClaim(token, Claims::getExpiration)
    }

    fun <T> extractClaim(token: String, claimsResolver: Function<Claims, T>): T {
        val claims = extractAllClaims(token)
        return claimsResolver.apply(claims)
    }

    private fun extractAllClaims(token: String): Claims {
        return Jwts.parserBuilder()
            .setSigningKey(secretKey)
            .build()
            .parseClaimsJws(token)
            .body
    }

    private fun isTokenExpired(token: String): Boolean {
        return extractExpiration(token).before(Date())
    }

    fun extractUserId(token: String): UUID? {
        return try {
            extractClaim(token) { claims -> claims["userId"] as UUID }
        } catch (e: Exception) {
            logger.error("Failed to extract userId from token: ${e.message}", e)
            null
        }
    }

    fun extractFullName(token: String): String? {
        return try {
            extractClaim(token) { claims -> claims["fullName"] as? String }
        } catch (e: Exception) {
            logger.error("Failed to extract fullName from token: ${e.message}", e)
            null
        }
    }

    fun extractProfilePicture(token: String): String? {
        return try {
            extractClaim(token) { claims -> claims["profilePicture"] as? String }
        } catch (e: Exception) {
            logger.error("Failed to extract profilePicture from token: ${e.message}", e)
            null
        }
    }

    fun extractCurrentLevel(token: String): JLPTLevel? {
        return try {
            extractClaim(token) { claims ->
                (claims["currentLevel"] as? String)?.let { JLPTLevel.valueOf(it) }
            }
        } catch (e: Exception) {
            logger.error("Failed to extract currentLevel from token: ${e.message}", e)
            null
        }
    }

    fun extractJlptGoal(token: String): JLPTLevel? {
        return try {
            extractClaim(token) { claims ->
                (claims["jlptGoal"] as? String)?.let { JLPTLevel.valueOf(it) }
            }
        } catch (e: Exception) {
            logger.error("Failed to extract jlptGoal from token: ${e.message}", e)
            null
        }
    }

    fun extractLastLogin(token: String): LocalDateTime? {
        return try {
            extractClaim(token) { claims ->
                (claims["lastLogin"] as? String)?.let { LocalDateTime.parse(it, dateTimeFormatter) }
            }
        } catch (e: Exception) {
            logger.error("Failed to extract lastLogin from token: ${e.message}", e)
            null
        }
    }
}