package io.github.ndtung723.nihongoit.learningservice.client

import org.slf4j.LoggerFactory
import org.springframework.beans.factory.annotation.Value
import org.springframework.http.HttpHeaders
import org.springframework.http.client.SimpleClientHttpRequestFactory
import org.springframework.stereotype.Component
import org.springframework.web.client.RestClient
import org.springframework.web.client.RestClientException
import java.time.Duration
import java.util.UUID

/**
 * Cross-service client for pushing in-app notifications. Calls
 * notification-service's internal endpoint guarded by the shared
 * `X-Internal-API-Key` header — production reads the secret from
 * INTERNAL_API_KEY env, dev defaults to blank which disables the endpoint
 * on the receiver side (notification simply not delivered, comment thread
 * still works).
 *
 * Failures are logged and swallowed: notification dispatch must never
 * break the user-facing flow that triggered it. Aggressive 2s connect /
 * 5s read timeout cap the worst-case latency penalty on the comment-create
 * response path.
 *
 * Base URL defaults to the dev hostport `localhost:8089` so this works
 * without Spring Cloud LoadBalancer setup. Production deployments override
 * via `NOTIFICATION_BASE_URL` env (e.g. `http://notification-service`
 * resolvable through the Eureka registry or an ingress DNS).
 */
@Component
class NotificationClient(
    @Value("\${app.notification.base-url:http://localhost:8089}")
    private val baseUrl: String,
    @Value("\${app.internal-api-key:}")
    private val internalApiKey: String,
) {
    private val log = LoggerFactory.getLogger(NotificationClient::class.java)
    private val rest =
        RestClient
            .builder()
            .baseUrl(baseUrl)
            .requestFactory(
                SimpleClientHttpRequestFactory().apply {
                    setConnectTimeout(Duration.ofSeconds(2))
                    setReadTimeout(Duration.ofSeconds(5))
                },
            ).build()

    fun pushCommentReply(
        recipientUserId: UUID,
        actorFullName: String,
        replyPreview: String,
        vocabId: UUID,
        commentId: UUID,
    ) {
        if (internalApiKey.isBlank()) {
            log.debug("internal-api-key not configured — skipping notification push")
            return
        }
        val preview = replyPreview.take(120).let { if (replyPreview.length > 120) "$it…" else it }
        val body =
            mapOf(
                "userId" to recipientUserId.toString(),
                "title" to "$actorFullName đã trả lời bình luận của bạn",
                "message" to preview,
                "type" to "COMMENT_REPLY",
                "actionUrl" to "/vocabulary/$vocabId#comment-$commentId",
                "priorityLevel" to 1,
            )
        try {
            rest
                .post()
                .uri("/api/v1/notify/internal/notifications")
                .header(HttpHeaders.CONTENT_TYPE, "application/json")
                .header("X-Internal-API-Key", internalApiKey)
                .body(body)
                .retrieve()
                .toBodilessEntity()
        } catch (e: RestClientException) {
            log.warn(
                "notification.dispatch.failed recipient={} reason={}",
                recipientUserId,
                e.message,
            )
        }
    }
}
