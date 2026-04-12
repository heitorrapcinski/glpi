package com.glpi.gateway.filter;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.cloud.gateway.filter.GatewayFilterChain;
import org.springframework.cloud.gateway.filter.GlobalFilter;
import org.springframework.core.Ordered;
import org.springframework.core.io.buffer.DataBuffer;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.server.reactive.ServerHttpResponse;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;

import java.nio.charset.StandardCharsets;
import java.util.Arrays;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * Global filter that validates the App-Token header when present.
 * If the App-Token header is present but invalid, returns HTTP 401.
 * If the header is absent, the filter passes through (JWT validation handles auth).
 */
@Component
public class AppTokenValidationFilter implements GlobalFilter, Ordered {

    private static final Logger log = LoggerFactory.getLogger(AppTokenValidationFilter.class);
    private static final String APP_TOKEN_HEADER = "App-Token";

    private final Set<String> validTokens;

    public AppTokenValidationFilter(@Value("${gateway.app-tokens.valid-tokens:}") String validTokensConfig) {
        this.validTokens = Arrays.stream(validTokensConfig.split(","))
                .map(String::trim)
                .filter(t -> !t.isBlank())
                .collect(Collectors.toSet());
    }

    @Override
    public int getOrder() {
        return -300; // Run before JWT filter
    }

    @Override
    public Mono<Void> filter(ServerWebExchange exchange, GatewayFilterChain chain) {
        String appToken = exchange.getRequest().getHeaders().getFirst(APP_TOKEN_HEADER);

        // If no App-Token header, skip this filter
        if (appToken == null) {
            return chain.filter(exchange);
        }

        // Validate the token
        if (!validTokens.contains(appToken)) {
            log.debug("Invalid App-Token presented");
            return writeErrorResponse(exchange);
        }

        return chain.filter(exchange);
    }

    private Mono<Void> writeErrorResponse(ServerWebExchange exchange) {
        ServerHttpResponse response = exchange.getResponse();
        response.setStatusCode(HttpStatus.UNAUTHORIZED);
        response.getHeaders().setContentType(MediaType.APPLICATION_JSON);

        String body = "{\"errorCode\":\"INVALID_APP_TOKEN\"}";
        DataBuffer buffer = response.bufferFactory()
                .wrap(body.getBytes(StandardCharsets.UTF_8));
        return response.writeWith(Mono.just(buffer));
    }
}
