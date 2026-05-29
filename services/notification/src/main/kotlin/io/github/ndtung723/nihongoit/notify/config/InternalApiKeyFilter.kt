package io.github.ndtung723.nihongoit.notify.config

import jakarta.servlet.FilterChain
import jakarta.servlet.http.HttpServletRequest
import jakarta.servlet.http.HttpServletResponse
import org.springframework.beans.factory.annotation.Value
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken
import org.springframework.security.core.authority.SimpleGrantedAuthority
import org.springframework.security.core.context.SecurityContextHolder
import org.springframework.stereotype.Component
import org.springframework.web.filter.OncePerRequestFilter

/**
 * Authenticates internal cross-service requests by matching the
 * `X-Internal-API-Key` header against a configured secret. Used by
 * learning-service when it pushes a COMMENT_REPLY notification — those
 * requests have no end-user JWT (they're created on behalf of the parent
 * comment's author, who may not be active right now).
 *
 * On match: installs a synthetic SYSTEM principal with ROLE_SERVICE so
 * `@PreAuthorize` and the rest of the security chain treat the request as
 * authenticated. On mismatch: passes through — the standard JWT chain runs
 * next and rejects the request if needed.
 */
@Component
class InternalApiKeyFilter(
    @Value("\${app.internal-api-key:}")
    private val expectedKey: String,
) : OncePerRequestFilter() {
    override fun shouldNotFilter(request: HttpServletRequest): Boolean = !request.requestURI.startsWith(INTERNAL_PATH)

    override fun doFilterInternal(
        request: HttpServletRequest,
        response: HttpServletResponse,
        chain: FilterChain,
    ) {
        val provided = request.getHeader("X-Internal-API-Key")
        if (expectedKey.isNotBlank() && provided == expectedKey) {
            val auth =
                UsernamePasswordAuthenticationToken(
                    "internal-service",
                    null,
                    listOf(SimpleGrantedAuthority("ROLE_SERVICE")),
                )
            SecurityContextHolder.getContext().authentication = auth
        }
        chain.doFilter(request, response)
    }

    companion object {
        const val INTERNAL_PATH = "/api/v1/notify/internal/"
    }
}
