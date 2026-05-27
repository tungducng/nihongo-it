package io.github.ndtung723.nihongoit.learningservice.dto

import jakarta.validation.constraints.NotBlank
import jakarta.validation.constraints.Size
import java.util.UUID

data class CreateCommentRequest(
    @field:NotBlank(message = "Bình luận không được để trống")
    @field:Size(min = 1, max = 2000, message = "Bình luận tối đa 2000 ký tự")
    val content: String,
    val parentCommentId: UUID? = null,
)

data class UpdateCommentRequest(
    @field:NotBlank
    @field:Size(min = 1, max = 2000)
    val content: String,
)

data class LikeResult(
    val likeCount: Int,
    val liked: Boolean,
)
