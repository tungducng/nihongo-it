package com.example.japanesitlearning.dto.auth

import com.example.japanesitlearning.dto.ResponseDto
import com.example.japanesitlearning.dto.ResponseType
import com.example.japanesitlearning.dto.user.UserDto
import com.fasterxml.jackson.annotation.JsonInclude

@JsonInclude(JsonInclude.Include.NON_NULL)
data class GetCurrentUserResponseDto(
    val status: ResponseType,
    val userInfo: UserDto? = null,
    val message: String? = null
) {
    constructor(responseDto: ResponseDto, userInfo: UserDto? = null) : this(
        status = responseDto.status, 
        userInfo = userInfo,
        message = responseDto.message
    )
}
