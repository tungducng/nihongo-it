package com.example.japanesitlearning.dto.vocabulary

import com.example.japanesitlearning.dto.ResponseDto
import com.fasterxml.jackson.annotation.JsonInclude
import com.fasterxml.jackson.annotation.JsonProperty

@JsonInclude(JsonInclude.Include.NON_NULL)
data class CreateVocabularyResponseDto(
    @JsonProperty("result")
    val result: ResponseDto,
)
