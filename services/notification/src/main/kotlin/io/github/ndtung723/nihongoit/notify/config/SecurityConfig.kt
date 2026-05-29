package io.github.ndtung723.nihongoit.notify.config

import io.github.ndtung723.nihongoit.common.security.GatewayHeaderAuthFilter
import io.github.ndtung723.nihongoit.common.security.JwtAuthenticationEntryPoint
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.security.config.annotation.web.builders.HttpSecurity
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity
import org.springframework.security.config.http.SessionCreationPolicy
import org.springframework.security.web.SecurityFilterChain
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter

@Configuration
@EnableWebSecurity
class SecurityConfig(
    private val gatewayHeaderAuthFilter: GatewayHeaderAuthFilter,
    private val internalApiKeyFilter: InternalApiKeyFilter,
    private val jwtAuthenticationEntryPoint: JwtAuthenticationEntryPoint,
) {
    @Bean
    fun filterChain(http: HttpSecurity): SecurityFilterChain {
        http
            .csrf { it.disable() }
            .sessionManagement { it.sessionCreationPolicy(SessionCreationPolicy.STATELESS) }
            .exceptionHandling { it.authenticationEntryPoint(jwtAuthenticationEntryPoint) }
            .authorizeHttpRequests { auth ->
                auth
                    .requestMatchers("/actuator/**")
                    .permitAll()
                    .requestMatchers("/v3/api-docs/**", "/swagger-ui/**", "/swagger-ui.html")
                    .permitAll()
                    .requestMatchers("/api/v1/notify/internal/**")
                    .hasRole("SERVICE")
                    .anyRequest()
                    .authenticated()
            }.addFilterBefore(internalApiKeyFilter, UsernamePasswordAuthenticationFilter::class.java)
            .addFilterBefore(gatewayHeaderAuthFilter, UsernamePasswordAuthenticationFilter::class.java)

        return http.build()
    }
}
