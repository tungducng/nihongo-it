package io.github.ndtung723.nihongoit.common.config

import io.github.ndtung723.nihongoit.common.security.AuthenticationUtils
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.data.domain.AuditorAware
import org.springframework.data.jpa.repository.config.EnableJpaAuditing
import java.util.Optional

/**
 * Wires Spring Data JPA auditing into any consuming service.
 *
 * Activated automatically when JPA is on the classpath. The `AuditorAware`
 * bean returns the current user ID (UUID string) extracted from the Gateway
 * header — see [AuthenticationUtils.currentUserId].
 *
 * Services can opt out by defining their own `AuditorAware<String>` bean,
 * which will replace this one.
 */
@Configuration
@ConditionalOnClass(name = ["org.springframework.data.jpa.repository.JpaRepository"])
@EnableJpaAuditing(auditorAwareRef = "auditorProvider")
class AuditConfig {
    @Bean
    fun auditorProvider(): AuditorAware<String> = AuditorAware { Optional.ofNullable(AuthenticationUtils.currentUserId()) }
}
