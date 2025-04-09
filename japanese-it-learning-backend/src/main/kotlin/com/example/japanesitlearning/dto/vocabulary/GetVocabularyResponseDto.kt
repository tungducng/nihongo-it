package com.example.japanesitlearning.dto.vocabulary

import com.example.japanesitlearning.dto.ResponseDto
import com.fasterxml.jackson.annotation.JsonProperty

class GetVocabularyResponseDto(
    @JsonProperty("result")
    val result: ResponseDto,
    @JsonProperty("data")
    val data: VocabularyDto,
)