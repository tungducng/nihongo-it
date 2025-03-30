package com.example.japanesitlearning.service

import com.example.japanesitlearning.entity.UserEntity
import java.util.UUID

interface UserService {
    fun findById(id: UUID): UserEntity
}