package io.github.ndtung723.nihongoit.learningservice.repository

import io.github.ndtung723.nihongoit.learningservice.entity.UserProgressEntity
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.stereotype.Repository
import java.util.UUID

@Repository
interface UserProgressRepository : JpaRepository<UserProgressEntity, UUID>
