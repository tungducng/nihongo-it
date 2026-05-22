package com.example.userservice.dto

// refreshToken is nullable so callers can omit the body and rely on the
// refresh_token cookie set by the gateway. AuthController.refreshToken accepts
// `@RequestBody(required = false)` and falls back to cookie when null.
data class RefreshTokenRequest(
    val refreshToken: String? = null,
)
