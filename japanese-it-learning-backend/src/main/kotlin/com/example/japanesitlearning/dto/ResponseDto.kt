package com.example.japanesitlearning.dto


import com.fasterxml.jackson.annotation.JsonInclude

@JsonInclude(JsonInclude.Include.NON_NULL) // Only include non-null fields in JSON
data class ResponseDto<T>(
    val status: ResponseStatus,
    val message: String? = null,
    val messageId: String? = null,           // Optional, excluded if null
    val errors: Map<String, String?>? = null // Optional, excluded if null
)