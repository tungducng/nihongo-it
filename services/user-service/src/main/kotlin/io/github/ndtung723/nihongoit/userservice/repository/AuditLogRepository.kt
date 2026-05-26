package io.github.ndtung723.nihongoit.userservice.repository

import io.github.ndtung723.nihongoit.userservice.entity.AuditLogEntity
import org.springframework.data.jpa.repository.JpaRepository
import java.util.UUID

interface AuditLogRepository : JpaRepository<AuditLogEntity, UUID>
