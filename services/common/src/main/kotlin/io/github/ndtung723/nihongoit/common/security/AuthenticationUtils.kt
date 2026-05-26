package io.github.ndtung723.nihongoit.common.security

import org.springframework.security.core.context.SecurityContextHolder
import org.springframework.web.context.request.RequestContextHolder
import org.springframework.web.context.request.ServletRequestAttributes
import java.util.UUID

/**
 * Shared helpers for reading the current authenticated user.
 *
 * Authoritative source: the principal placed into `SecurityContextHolder` by
 * [GatewayHeaderAuthFilter] (a String containing the user UUID). The gateway
 * also forwards `X-Email` and `X-Role` headers — readable via [currentEmail] /
 * [currentRole] when present on the request.
 *
 * Returns `null` when called outside an authenticated request (e.g. background
 * jobs, anonymous endpoints) — callers must handle `null` explicitly rather
 * than throwing, so this works in audit listeners on system-initiated writes.
 */
object AuthenticationUtils {
    fun currentUserId(): String? {
        val auth = SecurityContextHolder.getContext().authentication ?: return null
        val principal = auth.principal as? String ?: return null
        return principal.takeIf { it.isNotBlank() }
    }

    fun currentUserUuid(): UUID? =
        currentUserId()?.let {
            runCatching { UUID.fromString(it) }.getOrNull()
        }

    fun currentRole(): String? {
        val auth = SecurityContextHolder.getContext().authentication ?: return null
        return auth.authorities
            .asSequence()
            .mapNotNull { it.authority }
            .firstOrNull { it.startsWith("ROLE_") }
            ?.removePrefix("ROLE_")
    }

    // Gateway injects this as X-User-Email (matches X-User-Id, X-User-Role pattern).
    // Falls back to legacy X-Email for any leftover docker compose env.
    fun currentEmail(): String? = currentRequestHeader("X-User-Email") ?: currentRequestHeader("X-Email")

    private fun currentRequestHeader(name: String): String? {
        val attrs =
            RequestContextHolder.getRequestAttributes() as? ServletRequestAttributes
                ?: return null
        return attrs.request.getHeader(name)?.takeIf { it.isNotBlank() }
    }
}
