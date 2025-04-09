package com.example.japanesitlearning.dto.auth

import com.example.japanesitlearning.dto.ResponseDto
import com.example.japanesitlearning.dto.user.UserDto

data class GetCurrentUserResponseDto(
    val result: ResponseDto,
    val userInfo: UserDto,
)
