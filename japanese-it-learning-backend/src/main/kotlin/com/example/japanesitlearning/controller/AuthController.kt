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
import com.example.japanesitlearning.util.UserAuthUtil
import io.swagger.v3.oas.annotations.Operation
import io.swagger.v3.oas.annotations.Parameter
import io.swagger.v3.oas.annotations.media.Content
import io.swagger.v3.oas.annotations.media.Schema
import io.swagger.v3.oas.annotations.responses.ApiResponse
import io.swagger.v3.oas.annotations.responses.ApiResponses
import io.swagger.v3.oas.annotations.tags.Tag
import jakarta.validation.Valid
import org.slf4j.LoggerFactory
import org.springframework.http.HttpStatus
import org.springframework.http.MediaType
import org.springframework.http.ResponseEntity
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
@Tag(name = "Authentication", description = "API endpoints for user authentication, registration and profile management")
class AuthController(
    private val authenticationManager: AuthenticationManager,
    private val userRepository: UserRepository,
    private val roleRepository: RoleRepository,
    private val passwordEncoder: PasswordEncoder,
    private val jwtTokenUtil: JwtTokenUtil,
    private val userAuthUtil: UserAuthUtil,
) {
    private val logger = LoggerFactory.getLogger(AuthController::class.java)

    @PostMapping("/login", produces = [MediaType.APPLICATION_JSON_VALUE])
    @Operation(
        summary = "Authenticate user",
        description = "Authenticates a user with email and password, returns JWT token on success"
    )
    @ApiResponses(
        value = [
            ApiResponse(
                responseCode = "200",
                description = "Authentication successful",
                content = [Content(mediaType = "application/json", schema = Schema(implementation = LoginResponseDto::class))]
            ),
            ApiResponse(responseCode = "401", description = "Authentication failed - invalid credentials")
        ]
    )
    fun login(
        @Parameter(description = "Login credentials", required = true)
        @Valid @RequestBody request: LoginRequest
    ): LoginResponseDto {
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

        // Update lastLogin time
        val updatedUser = user.copy(lastLogin = LocalDateTime.now())
        userRepository.save(updatedUser)
        logger.debug("Updated lastLogin for user with email: ${request.email}")
        
        val token = jwtTokenUtil.generateToken(updatedUser)
        logger.debug("Login successful for user with email: ${request.email}")

        return LoginResponseDto(
            result = ResponseType.OK,
            token,
        )
    }

    @PostMapping("/signup", produces = [MediaType.APPLICATION_JSON_VALUE])
    @Operation(
        summary = "Register new user",
        description = "Creates a new user account with the provided details"
    )
    @ApiResponses(
        value = [
            ApiResponse(
                responseCode = "200",
                description = "Registration successful",
                content = [Content(mediaType = "application/json", schema = Schema(implementation = SignupResponseDto::class))]
            ),
            ApiResponse(responseCode = "400", description = "Invalid request data or email already in use")
        ]
    )
    fun register(
        @Parameter(description = "User registration details", required = true)
        @Valid @RequestBody request: SignupRequest
    ): SignupResponseDto {
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
                status = ResponseType.OK,
                message = "Registration successful"
            )
        } catch (ex: Exception) {
            logger.error("Registration error: ${ex.message}", ex)
            throw ex
        }
    }

    @GetMapping("/current", produces = [MediaType.APPLICATION_JSON_VALUE])
    @PreAuthFilter(hasAnyRole = ["user", "admin"])
    @Operation(
        summary = "Get current user information",
        description = "Retrieves detailed information about the currently authenticated user"
    )
    @ApiResponses(
        value = [
            ApiResponse(
                responseCode = "200",
                description = "User information retrieved successfully",
                content = [Content(mediaType = "application/json", schema = Schema(implementation = GetCurrentUserResponseDto::class))]
            ),
            ApiResponse(responseCode = "401", description = "Unauthorized - authentication required"),
            ApiResponse(responseCode = "404", description = "User not found")
        ]
    )
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
