package com.example.japanesitlearning.security

import com.example.japanesitlearning.dto.ResponseDto
import com.example.japanesitlearning.dto.ResponseStatus
import com.fasterxml.jackson.databind.ObjectMapper
import jakarta.servlet.http.HttpServletRequest
import jakarta.servlet.http.HttpServletResponse
import org.springframework.http.MediaType
import org.springframework.security.core.AuthenticationException
import org.springframework.security.web.AuthenticationEntryPoint
import org.springframework.stereotype.Component
import java.io.IOException

@Component
class JwtAuthenticationEntryPoint : AuthenticationEntryPoint {

    @Throws(IOException::class)
    override fun commence(
        request: HttpServletRequest,
        response: HttpServletResponse,
        authException: AuthenticationException
    ) {
        response.contentType = MediaType.APPLICATION_JSON_VALUE
        response.status = HttpServletResponse.SC_UNAUTHORIZED

        val responseDto = ResponseDto<Any>(
            status = ResponseStatus.NG,
            message = authException.message ?: "Invalid authentication token",
            messageId = "AUTH_001"
        )

        val mapper = ObjectMapper()
        mapper.writeValue(response.outputStream, responseDto)
    }
}