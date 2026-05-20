package com.example.common.exception

/**
 * Thrown when a requested entity / resource does not exist. Mapped to HTTP 404
 * by `GlobalExceptionHandler`. Use this in preference to `BusinessException`
 * when the failure mode is specifically "resource missing" so the client gets
 * the correct status code and can distinguish from validation / business
 * rule failures (which remain 400 via `BusinessException`).
 */
class NotFoundException(
    message: String,
) : RuntimeException(message)
