package com.example.userservice.util

import com.example.common.security.AuthenticationUtils
import org.springframework.stereotype.Component
import java.util.UUID

/**
 * Thin wrapper around [AuthenticationUtils] so existing call sites
 * (`userAuthUtil.getCurrentUserId()`) keep working. New code should call
 * `AuthenticationUtils.currentUserUuid()` directly.
 *
 * All auth state comes from the SecurityContext populated by
 * `GatewayHeaderAuthFilter` (X-User-Id / X-Role / X-Email headers
 * injected by the API gateway after JWT validation). JWT re-parsing
 * has been removed — the gateway is the single validation point.
 */
@Component
class UserAuthUtil {
    fun getCurrentUserId(): UUID? = AuthenticationUtils.currentUserUuid()

    fun getCurrentRoleId(): Int? =
        when (AuthenticationUtils.currentRole()) {
            "ADMIN" -> 1
            "USER" -> 2
            else -> null
        }

    fun getCurrentEmail(): String? = AuthenticationUtils.currentEmail()

    fun isAuthenticated(): Boolean = AuthenticationUtils.currentUserId() != null
}

/**
 * Optional interface for principal types that expose a UUID directly.
 * Retained for backward compatibility with any custom UserDetails impl.
 */
interface CustomUserDetails {
    val userId: UUID
}
