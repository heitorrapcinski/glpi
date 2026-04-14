package com.glpi.gateway.filter;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.cloud.gateway.filter.GatewayFilterChain;
import org.springframework.cloud.gateway.filter.GlobalFilter;
import org.springframework.core.Ordered;
import org.springframework.http.server.reactive.ServerHttpRequest;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;

import java.time.Instant;

/**
 * Global filter that logs each request with timestamp, method, path, user ID, response status, and latency.
 */
@Component
public class RequestLoggingFilter implements GlobalFilter, Ordered {

    private static final Logger log = LoggerFactory.getLogger(RequestLoggingFilter.class);

    @Override
    public int getOrder() {
        return -100; // Run after JwtAuthenticationFilter (-200) so X-User-Id is available
    }

    @Override
    public Mono<Void> filter(ServerWebExchange exchange, GatewayFilterChain chain) {
        long startTime = Instant.now().toEpochMilli();
        ServerHttpRequest request = exchange.getRequest();

        String method = request.getMethod().name();
        String path = request.getURI().getPath();

        return chain.filter(exchange).doFinally(signalType -> {
            long latencyMs = Instant.now().toEpochMilli() - startTime;
            int statusCode = exchange.getResponse().getStatusCode() != null
                    ? exchange.getResponse().getStatusCode().value()
                    : 0;

            // Read X-User-Id from the (possibly mutated) request on the exchange
            String userId = exchange.getRequest().getHeaders().getFirst("X-User-Id");

            log.info("timestamp={} method={} path={} userId={} status={} latencyMs={}",
                    Instant.now(), method, path,
                    userId != null && !userId.isBlank() ? userId : "anonymous",
                    statusCode, latencyMs);
        });
    }
}
