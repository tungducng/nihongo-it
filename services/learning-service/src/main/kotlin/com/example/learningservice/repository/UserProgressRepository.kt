package com.example.learningservice.repository

import com.example.learningservice.entity.UserProgressEntity
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.stereotype.Repository
import java.util.UUID

@Repository
interface UserProgressRepository : JpaRepository<UserProgressEntity, UUID>
