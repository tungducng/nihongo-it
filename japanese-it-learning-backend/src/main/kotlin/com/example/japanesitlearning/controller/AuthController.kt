package com.example.japanesitlearning.controller

import com.example.japanesitlearning.exception.BusinessException
import com.example.japanesitlearning.dto.LoginRequest
import com.example.japanesitlearning.dto.LoginResponseDto
import com.example.japanesitlearning.dto.ResponseDto
import com.example.japanesitlearning.dto.ResponseStatus
import com.example.japanesitlearning.dto.SignupRequest
import com.example.japanesitlearning.dto.SignupResponseDto
import com.example.japanesitlearning.dto.UserInfoResponseDto
import com.example.japanesitlearning.entity.UserEntity
import com.example.japanesitlearning.repository.UserRepository
import com.example.japanesitlearning.security.JwtTokenUtil
import com.example.japanesitlearning.security.PreAuthFilter
import com.example.japanesitlearning.security.UserAuthUtil
import jakarta.validation.Valid
import org.springframework.security.authentication.AuthenticationManager
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken
import org.springframework.security.core.context.SecurityContextHolder
import org.springframework.security.crypto.password.PasswordEncoder
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController
import org.slf4j.LoggerFactory
import org.springframework.security.core.annotation.AuthenticationPrincipal
import org.springframework.security.core.userdetails.UserDetails
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.RequestHeader

@RestController
@RequestMapping("/api/v1/auth")
class AuthController(
    private val authenticationManager: AuthenticationManager,
    private val userRepository: UserRepository,
    private val passwordEncoder: PasswordEncoder,
    private val jwtTokenUtil: JwtTokenUtil,
    private val userAuthUtil: UserAuthUtil
) {
    private val logger = LoggerFactory.getLogger(AuthController::class.java)

    @PostMapping("/login")
    fun login(@Valid @RequestBody loginRequest: LoginRequest): LoginResponseDto {
        logger.debug("Login attempt for user: ${loginRequest.username}")

        val user = userRepository.findByUsername(loginRequest.username)
        if (user == null) {
            logger.debug("Login failed: User ${loginRequest.username} not found")
            return LoginResponseDto(
                result = ResponseStatus.NG,
            )
        }

        val authentication = authenticationManager.authenticate(
            UsernamePasswordAuthenticationToken(loginRequest.username, loginRequest.password)
        )

        SecurityContextHolder.getContext().authentication = authentication

        val token = jwtTokenUtil.generateToken(user)
        logger.debug("Login successful for user: ${loginRequest.username}")

        return LoginResponseDto(
                result = ResponseStatus.OK,
                token
            )
    }

    @PostMapping("/signup")
    fun register(@Valid @RequestBody signupRequest: SignupRequest): SignupResponseDto {
        logger.debug("Signup attempt for username: ${signupRequest.username}")

        if (userRepository.existsByUsername(signupRequest.username)) {
            logger.debug("Signup failed: Username ${signupRequest.username} already exists")
            throw BusinessException("Username is already taken")
        }

        if (userRepository.existsByEmail(signupRequest.email)) {
            logger.debug("Signup failed: Email ${signupRequest.email} already exists")
            throw BusinessException("Email is already in use")
        }

        val user = UserEntity(
            username = signupRequest.username,
            email = signupRequest.email,
            password = passwordEncoder.encode(signupRequest.password),
            roleId = signupRequest.roleId ?: 2 // Default to ROLE_USER if not specified
        )

        userRepository.save(user)
        logger.debug("User registered successfully: ${signupRequest.username}")

        return SignupResponseDto(
            ResponseDto(
                status = ResponseStatus.OK
            )
        )
    }

    @PreAuthFilter(hasAnyRole = ["ADMIN", "USER"])
    @GetMapping("/current")
    fun getUserInfo(): UserInfoResponseDto {
        val username = userAuthUtil.getCurrentUsername()
            ?: throw BusinessException("User not found: Invalid or missing token")

        val email = userAuthUtil.getCurrentEmail()
            ?: throw BusinessException("Cannot extract email: Invalid token")

        val roleId = userAuthUtil.getCurrentRoleId()
            ?: throw BusinessException("Cannot extract roleId: Invalid token")

        return UserInfoResponseDto(
            username,
            email,
            roleId
        )
    }
}
