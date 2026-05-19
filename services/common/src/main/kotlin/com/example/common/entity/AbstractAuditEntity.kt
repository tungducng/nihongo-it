package com.example.common.entity

import jakarta.persistence.Column
import jakarta.persistence.EntityListeners
import jakarta.persistence.MappedSuperclass
import org.springframework.data.annotation.CreatedBy
import org.springframework.data.annotation.CreatedDate
import org.springframework.data.annotation.LastModifiedBy
import org.springframework.data.annotation.LastModifiedDate
import org.springframework.data.jpa.domain.support.AuditingEntityListener
import java.time.LocalDateTime

/**
 * Base entity providing auditing columns.
 *
 * Services consuming this superclass must:
 *  1. Add `org.springframework.boot:spring-boot-starter-data-jpa` (already on
 *     services that use JPA).
 *  2. Register `@EnableJpaAuditing` via [com.example.common.config.AuditConfig]
 *     (or by importing it directly).
 *  3. Provide an `AuditorAware<String>` bean — [AuditConfig.auditorProvider]
 *     reads `X-User-Id` populated by `GatewayHeaderAuthFilter`.
 *
 * Columns map to `created_at`, `created_by`, `updated_at`, `updated_by` —
 * matches the `TIMESTAMP` / `LocalDateTime` shape used by existing Flyway
 * migrations across user / learning / notification services.
 */
@MappedSuperclass
@EntityListeners(AuditingEntityListener::class)
abstract class AbstractAuditEntity {
    @CreatedDate
    @Column(name = "created_at", nullable = false, updatable = false)
    var createdAt: LocalDateTime? = null

    @CreatedBy
    @Column(name = "created_by", updatable = false, length = 64)
    var createdBy: String? = null

    @LastModifiedDate
    @Column(name = "updated_at", nullable = false)
    var updatedAt: LocalDateTime? = null

    @LastModifiedBy
    @Column(name = "updated_by", length = 64)
    var updatedBy: String? = null
}
