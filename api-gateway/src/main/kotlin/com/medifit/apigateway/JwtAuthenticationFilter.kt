package com.medifit.apigateway

import org.slf4j.LoggerFactory
import org.springframework.cloud.gateway.filter.GatewayFilter
import org.springframework.cloud.gateway.filter.GatewayFilterChain
import org.springframework.http.HttpHeaders
import org.springframework.http.HttpStatus
import org.springframework.stereotype.Component
import org.springframework.web.server.ServerWebExchange
import reactor.core.publisher.Mono

@Component
class JwtTokenAuthenticationFilter : GatewayFilter {
    private val logger = LoggerFactory.getLogger(this::class.java)

    override fun filter(exchange: ServerWebExchange, chain: GatewayFilterChain): Mono<Void> {
        val request = exchange.request

        val authHeader = request.headers.getOrEmpty(HttpHeaders.AUTHORIZATION)

        if (authHeader.isEmpty()) {
            logger.warn("No Authorization header.")
            val response = exchange.response
            response.statusCode = HttpStatus.UNAUTHORIZED
            return response.setComplete()
        }

        return chain.filter(exchange)
    }

}
