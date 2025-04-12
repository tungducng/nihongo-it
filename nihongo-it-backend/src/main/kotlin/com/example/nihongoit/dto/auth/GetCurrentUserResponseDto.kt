package com.example.nihongoit.dto.auth

import com.example.nihongoit.dto.ResponseDto
import com.example.nihongoit.dto.ResponseType
import com.example.nihongoit.dto.user.UserDto
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
