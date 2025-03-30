package com.example.japanesitlearning.service.impl

import com.example.japanesitlearning.entity.UserEntity
import com.example.japanesitlearning.repository.UserRepository
import com.example.japanesitlearning.service.UserService
import org.springframework.stereotype.Service
import java.util.UUID

@Service
class UserServiceImpl(
    private val userRepository: UserRepository
) : UserService {
    override fun findById(id: UUID): UserEntity {
        TODO("Not yet implemented")
    }

}