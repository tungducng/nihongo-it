package com.example.userservice.dto

import com.fasterxml.jackson.annotation.JsonProperty

class GetVocabularyResponseDto(
    @JsonProperty("result")
    val result: ResponseDto,
    @JsonProperty("data")
    val data: VocabularyDto,
)