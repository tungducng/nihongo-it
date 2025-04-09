package com.example.japanesitlearning.controller

import com.example.japanesitlearning.dto.ResponseDto
import com.example.japanesitlearning.dto.ResponseType
import com.example.japanesitlearning.dto.user.UserDto
import com.example.japanesitlearning.dto.auth.GetCurrentUserResponseDto
import com.example.japanesitlearning.dto.auth.LoginRequest
import com.example.japanesitlearning.dto.auth.LoginResponseDto
import com.example.japanesitlearning.dto.auth.SignupRequest
import com.example.japanesitlearning.dto.auth.SignupResponseDto
import com.example.japanesitlearning.entity.UserEntity
import com.example.japanesitlearning.exception.BusinessException
import com.example.japanesitlearning.repository.RoleRepository
import com.example.japanesitlearning.repository.UserRepository
import com.example.japanesitlearning.security.JwtTokenUtil
import com.example.japanesitlearning.security.PreAuthFilter
import com.example.japanesitlearning.security.UserAuthUtil
import jakarta.validation.Valid
import org.slf4j.LoggerFactory
import org.springframework.security.authentication.AuthenticationManager
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken
import org.springframework.security.core.context.SecurityContextHolder
import org.springframework.security.crypto.password.PasswordEncoder
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController
import java.time.LocalDateTime

@RestController
@RequestMapping("/api/v1/auth")
class AuthController(
    private val authenticationManager: AuthenticationManager,
    private val userRepository: UserRepository,
    private val roleRepository: RoleRepository,
    private val passwordEncoder: PasswordEncoder,
    private val jwtTokenUtil: JwtTokenUtil,
    private val userAuthUtil: UserAuthUtil,
) {
    private val logger = LoggerFactory.getLogger(AuthController::class.java)

    @PostMapping("/login")
    fun login(@Valid @RequestBody request: LoginRequest): LoginResponseDto {
        logger.debug("Login attempt for email: ${request.email}")

        val user = userRepository.findByEmail(request.email)
        if (user == null) {
            logger.debug("Login failed: User with email ${request.email} not found")
            return LoginResponseDto(
                result = ResponseType.NG,
            )
        }

        val authentication = authenticationManager.authenticate(
            UsernamePasswordAuthenticationToken(request.email, request.password),
        )

        SecurityContextHolder.getContext().authentication = authentication

        val token = jwtTokenUtil.generateToken(user)
        logger.debug("Login successful for user with email: ${request.email}")

        return LoginResponseDto(
            result = ResponseType.OK,
            token,
        )
    }

    @PostMapping("/signup")
    fun register(@Valid @RequestBody request: SignupRequest): SignupResponseDto {
        try {
            // Check if email already exists
            if (userRepository.existsByEmail(request.email)) {
                logger.debug("Signup failed: Email ${request.email} already exists")
                throw BusinessException("Email is already in use")
            }

            // Find the user role
            val role = roleRepository.findByRoleId(2)
                ?: throw BusinessException("Role not found")

            // Create user entity
            val user = UserEntity(
                email = request.email,
                password = passwordEncoder.encode(request.password),
                fullName = request.fullName,
                profilePicture = request.profilePicture,
                currentLevel = request.currentLevel,
                jlptGoal = request.jlptGoal,
                lastLogin = LocalDateTime.now(),
                role = role,
            )

            // Save user
            val savedUser = userRepository.save(user)
            logger.debug("User registered successfully with ID: ${savedUser.userId}")

            return SignupResponseDto(
                ResponseDto(
                    status = ResponseType.OK,
                ),
            )
        } catch (ex: Exception) {
            logger.error("Registration error: ${ex.message}", ex)
            throw ex
        }
    }

    @GetMapping("/current")
    @PreAuthFilter(hasAnyRole = ["user", "admin"])
    fun getCurrentUser(): GetCurrentUserResponseDto {
        try {
            // Get current user ID
            val userId = userAuthUtil.getCurrentUserId()
                ?: throw BusinessException("Cannot extract userId: Invalid token")

            // Fetch full user details from database
            val user = userRepository.findById(userId)
                .orElseThrow { BusinessException("User not found") }

            // Map to response DTO
            val userInfo = UserDto(
                userId = user.userId!!,
                email = user.email,
                fullName = user.fullName,
                roleId = user.role.roleId,
                profilePicture = user.profilePicture,
                currentLevel = user.currentLevel,
                jlptGoal = user.jlptGoal,
                lastLogin = user.lastLogin,
            )

            logger.debug("Successfully retrieved user info for user ID: {}", userId)

            return GetCurrentUserResponseDto(
                ResponseDto(
                    status = ResponseType.OK,
                ),
                userInfo = userInfo,
            )
        } catch (e: Exception) {
            logger.error("Error retrieving user info: ${e.message}", e)
            throw e
        }
    }
}
