package com.example.nihongoit.dto.vocabulary

import com.example.nihongoit.dto.ResponseDto
import com.fasterxml.jackson.annotation.JsonProperty

class GetVocabularyResponseDto(
    @JsonProperty("result")
    val result: ResponseDto,
    @JsonProperty("data")
    val data: VocabularyDto,
)