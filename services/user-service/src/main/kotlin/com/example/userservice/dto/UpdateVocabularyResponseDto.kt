package com.example.userservice.dto

import com.fasterxml.jackson.annotation.JsonProperty

class UpdateVocabularyResponseDto (
    @JsonProperty("result")
    val result: ResponseDto,
    @JsonProperty("data")
    val data: VocabularyDto
)