package io.github.ndtung723.nihongoit.apigateway.filter

import org.slf4j.LoggerFactory
import org.springframework.cloud.gateway.filter.GatewayFilterChain
import org.springframework.cloud.gateway.filter.GlobalFilter
import org.springframework.core.Ordered
import org.springframework.stereotype.Component
import org.springframework.web.server.ServerWebExchange
import reactor.core.publisher.Mono
import java.util.UUID

const val CORRELATION_ID_HEADER = "X-Correlation-Id"

@Component
class CorrelationIdGlobalFilter :
    GlobalFilter,
    Ordered {
    companion object {
        private const val FILTER_ORDER = -200
    }

    private val logger = LoggerFactory.getLogger(CorrelationIdGlobalFilter::class.java)

    override fun filter(
        exchange: ServerWebExchange,
        chain: GatewayFilterChain,
    ): Mono<Void> {
        val correlationId =
            exchange.request.headers.getFirst(CORRELATION_ID_HEADER)
                ?: UUID.randomUUID().toString()

        logger.debug(
            "correlationId={} method={} path={}",
            correlationId,
            exchange.request.method,
            exchange.request.path.value(),
        )

        val mutatedRequest =
            exchange.request
                .mutate()
                .header(CORRELATION_ID_HEADER, correlationId)
                .build()

        // Set the response header BEFORE commit. A previous version of this
        // filter set the header in `.then { ... }` after the upstream chain
        // finished — by that point the gateway had already flushed the
        // response headers/body, and writing to the immutable header map
        // raised UnsupportedOperationException. Reactor would then close the
        // socket WITHOUT the final 0-length chunked terminator, making axios
        // and Playwright's APIRequestContext abort while reading the body
        // (the BE's "half-closed chunked response" symptom we've been chasing).
        exchange.response.beforeCommit {
            exchange.response.headers.set(CORRELATION_ID_HEADER, correlationId)
            Mono.empty<Void>()
        }
        return chain.filter(exchange.mutate().request(mutatedRequest).build())
    }

    override fun getOrder(): Int = FILTER_ORDER
}
