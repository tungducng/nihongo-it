package com.example.japanesitlearning.exception

import com.example.japanesitlearning.dto.ResponseDto
import com.example.japanesitlearning.dto.ResponseStatus
import io.jsonwebtoken.ExpiredJwtException
import io.jsonwebtoken.MalformedJwtException
import io.jsonwebtoken.UnsupportedJwtException
import io.jsonwebtoken.security.SignatureException
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.security.access.AccessDeniedException
import org.springframework.security.authentication.BadCredentialsException
import org.springframework.security.authentication.InsufficientAuthenticationException
import org.springframework.security.core.AuthenticationException
import org.springframework.validation.FieldError
import org.springframework.web.bind.MethodArgumentNotValidException
import org.springframework.web.bind.annotation.ExceptionHandler
import org.springframework.web.bind.annotation.RestControllerAdvice
import org.slf4j.LoggerFactory
import org.springframework.web.HttpRequestMethodNotSupportedException
import org.springframework.web.servlet.resource.NoResourceFoundException

@RestControllerAdvice
class GlobalExceptionHandler {

    private val logger = LoggerFactory.getLogger(GlobalExceptionHandler::class.java)

    // Handle validation errors (e.g., invalid DTO fields)
    @ExceptionHandler(MethodArgumentNotValidException::class)
    fun handleValidationExceptions(ex: MethodArgumentNotValidException): ResponseEntity<*> {
        val errors = mutableMapOf<String, String?>()
        ex.bindingResult.allErrors.forEach { error ->
            val fieldName = (error as FieldError).field
            val errorMessage = error.defaultMessage
            errors[fieldName] = errorMessage
        }
        logger.warn("Validation failed: $errors")
        return ResponseEntity(
            ResponseDto<Any>(
                status = ResponseStatus.NG,
                errors = errors
            ),
            HttpStatus.BAD_REQUEST
        )
    }

    // Handle business exceptions
    @ExceptionHandler(BusinessException::class)
    fun handleBusinessException(ex: BusinessException): ResponseEntity<*> {
        logger.warn("Business exception: ${ex.message}")
        return ResponseEntity(
            ResponseDto<Any>(
                status = ResponseStatus.NG,
                message = ex.message ?: "An error occurred",
                messageId = "BUSINESS_ERROR_001"
            ),
            HttpStatus.BAD_REQUEST
        )
    }

    // Handle JWT expired exception
    @ExceptionHandler(ExpiredJwtException::class)
    fun handleExpiredJwtException(ex: ExpiredJwtException): ResponseEntity<*> {
        logger.warn("JWT token expired: ${ex.message}")
        return ResponseEntity(
            ResponseDto<Any>(
                status = ResponseStatus.NG,
                message = "Authentication token has expired",
                messageId = "AUTH_ERROR_001"
            ),
            HttpStatus.FORBIDDEN
        )
    }

    // Handle JWT signature exception
    @ExceptionHandler(SignatureException::class)
    fun handleJwtSignatureException(ex: SignatureException): ResponseEntity<*> {
        logger.warn("Invalid JWT signature: ${ex.message}")
        return ResponseEntity(
            ResponseDto<Any>(
                status = ResponseStatus.NG,
                message = "Invalid authentication token signature",
                messageId = "AUTH_ERROR_002"
            ),
            HttpStatus.FORBIDDEN
        )
    }

    // Handle malformed JWT exception
    @ExceptionHandler(MalformedJwtException::class)
    fun handleMalformedJwtException(ex: MalformedJwtException): ResponseEntity<*> {
        logger.warn("Malformed JWT token: ${ex.message}")
        return ResponseEntity(
            ResponseDto<Any>(
                status = ResponseStatus.NG,
                message = "Malformed authentication token",
                messageId = "AUTH_ERROR_003"
            ),
            HttpStatus.FORBIDDEN
        )
    }

    // Handle unsupported JWT exception
    @ExceptionHandler(UnsupportedJwtException::class)
    fun handleUnsupportedJwtException(ex: UnsupportedJwtException): ResponseEntity<*> {
        logger.warn("Unsupported JWT token: ${ex.message}")
        return ResponseEntity(
            ResponseDto<Any>(
                status = ResponseStatus.NG,
                message = "Unsupported authentication token format",
                messageId = "AUTH_ERROR_004"
            ),
            HttpStatus.FORBIDDEN
        )
    }

    // Handle access denied exception (insufficient privileges)
    @ExceptionHandler(AccessDeniedException::class)
    fun handleAccessDeniedException(ex: AccessDeniedException): ResponseEntity<*> {
        logger.warn("Access denied: ${ex.message}")
        return ResponseEntity(
            ResponseDto<Any>(
                status = ResponseStatus.NG,
                message = "Access denied: Insufficient privileges",
                messageId = "AUTH_ERROR_005"
            ),
            HttpStatus.FORBIDDEN
        )
    }

    // Handle authentication exceptions
    @ExceptionHandler(AuthenticationException::class)
    fun handleAuthenticationException(ex: AuthenticationException): ResponseEntity<*> {
        val message = when (ex) {
            is BadCredentialsException -> "Invalid username or password"
            is InsufficientAuthenticationException -> "Authentication required"
            else -> "Authentication failed"
        }

        logger.warn("Authentication exception: ${ex.message}")
        return ResponseEntity(
            ResponseDto<Any>(
                status = ResponseStatus.NG,
                message = message,
                messageId = "AUTH_ERROR_006"
            ),
            HttpStatus.UNAUTHORIZED
        )
    }

    // Add this to your GlobalExceptionHandler class
    @ExceptionHandler(NoResourceFoundException::class)
    fun handleNoResourceFoundException(ex: NoResourceFoundException): ResponseEntity<*> {
        val resourcePath = ex.message?.replace("No static resource ", "") ?: "unknown"
        val message = "Resource not found: $resourcePath"

        logger.warn(message)
        return ResponseEntity(
            ResponseDto<Any>(
                status = ResponseStatus.NG,
                message = message,
                messageId = "RESOURCE_ERROR_001"
            ),
            HttpStatus.NOT_FOUND
        )
    }

    // Add this to your GlobalExceptionHandler class
    @ExceptionHandler(HttpRequestMethodNotSupportedException::class)
    fun handleMethodNotSupportedException(ex: HttpRequestMethodNotSupportedException): ResponseEntity<*> {
        val supportedMethods = ex.supportedMethods?.joinToString(", ") ?: "none"
        val message = "Request method '${ex.method}' is not supported. Supported methods: $supportedMethods"

        logger.warn(message)
        return ResponseEntity(
            ResponseDto<Any>(
                status = ResponseStatus.NG,
                message = message,
                messageId = "METHOD_ERROR_001"
            ),
            HttpStatus.METHOD_NOT_ALLOWED
        )
    }

    // Handle all other exceptions
    @ExceptionHandler(Exception::class)
    fun handleGenericException(ex: Exception): ResponseEntity<*> {
        logger.error("Unexpected error occurred: ${ex.message}", ex)
        return ResponseEntity(
            ResponseDto<Any>(
                status = ResponseStatus.NG,
                message = "An unexpected error occurred",
                messageId = "SERVER_ERROR_001"
            ),
            HttpStatus.INTERNAL_SERVER_ERROR
        )
    }
}