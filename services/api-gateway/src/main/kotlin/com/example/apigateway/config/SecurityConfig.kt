package com.example.apigateway.config

import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.security.config.annotation.web.reactive.EnableWebFluxSecurity
import org.springframework.security.config.web.server.ServerHttpSecurity
import org.springframework.security.web.server.SecurityWebFilterChain
import org.springframework.web.cors.CorsConfiguration
import org.springframework.web.cors.reactive.CorsWebFilter
import org.springframework.web.cors.reactive.UrlBasedCorsConfigurationSource
import org.springframework.http.HttpMethod

@Configuration
@EnableWebFluxSecurity
class SecurityConfig {

    @Bean
    fun springSecurityFilterChain(http: ServerHttpSecurity): SecurityWebFilterChain {
        return http
            .csrf { it.disable() }
            .cors { it.disable() }  // Disable Spring Security CORS, we'll use our custom CorsWebFilter
            .authorizeExchange {
                it.pathMatchers(HttpMethod.OPTIONS, "/**").permitAll()  // Always allow OPTIONS requests
                it.pathMatchers("/**").permitAll()  // Allow all requests
            }
            .build()
    }

    @Bean
    fun corsWebFilter(): CorsWebFilter {
        val corsConfig = CorsConfiguration()
        
        // Specify allowed origins - add any additional origins you need
        corsConfig.allowedOrigins = listOf("http://localhost:5173", "http://localhost:3000", "https://nihongo-it.com")
        
        // Allow all common methods
        corsConfig.addAllowedMethod("*")
        
        // Allow all headers
        corsConfig.addAllowedHeader("*")
        
        // Expose common response headers
        corsConfig.addExposedHeader("Authorization")
        corsConfig.addExposedHeader("Content-Disposition")
        
        // Allow cookies for authenticated requests
        corsConfig.allowCredentials = true
        
        corsConfig.maxAge = 3600L  // 1 hour

        val source = UrlBasedCorsConfigurationSource()
        source.registerCorsConfiguration("/**", corsConfig)
        return CorsWebFilter(source)
    }
} 