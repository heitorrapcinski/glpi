package com.glpi.gateway.filter;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.glpi.gateway.security.InMemoryTokenBlocklist;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.ExpiredJwtException;
import io.jsonwebtoken.JwtException;
import io.jsonwebtoken.Jwts;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.cloud.gateway.filter.GatewayFilterChain;
import org.springframework.cloud.gateway.filter.GlobalFilter;
import org.springframework.core.Ordered;
import org.springframework.core.io.buffer.DataBuffer;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.server.reactive.ServerHttpRequest;
import org.springframework.http.server.reactive.ServerHttpResponse;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;

import java.nio.charset.StandardCharsets;
import java.security.PublicKey;
import java.util.Base64;
import java.util.List;
import java.util.Map;

/**
 * Global filter that validates RS256 JWT tokens on all requests except public endpoints.
 * On valid JWT: extracts claims and forwards as X-User-Id, X-Entity-Id, X-Profile-Id, X-User-Rights headers.
 * On invalid/expired JWT: returns HTTP 401 with JSON error body.
 */
@Component
public class JwtAuthenticationFilter implements GlobalFilter, Ordered {

    private static final Logger log = LoggerFactory.getLogger(JwtAuthenticationFilter.class);

    /** Public endpoints that bypass JWT validation. */
    private static final List<String> PUBLIC_PATHS = List.of(
            "/auth/login",
            "/auth/refresh"
    );

    private final PublicKey jwtPublicKey;
    private final InMemoryTokenBlocklist tokenBlocklist;
    private final ObjectMapper objectMapper;

    public JwtAuthenticationFilter(PublicKey jwtPublicKey,
                                   InMemoryTokenBlocklist tokenBlocklist,
                                   ObjectMapper objectMapper) {
        this.jwtPublicKey = jwtPublicKey;
        this.tokenBlocklist = tokenBlocklist;
        this.objectMapper = objectMapper;
    }

    @Override
    public int getOrder() {
        return -200; // Run before rate limiting and routing
    }

    @Override
    public Mono<Void> filter(ServerWebExchange exchange, GatewayFilterChain chain) {
        String path = exchange.getRequest().getURI().getPath();

        // Skip filter for public endpoints
        if (isPublicPath(path)) {
            return chain.filter(exchange);
        }

        String authHeader = exchange.getRequest().getHeaders().getFirst(HttpHeaders.AUTHORIZATION);
        if (authHeader == null || !authHeader.startsWith("Bearer ")) {
            return writeErrorResponse(exchange, HttpStatus.UNAUTHORIZED, "TOKEN_INVALID");
        }

        String token = authHeader.substring(7);

        try {
            Claims claims = Jwts.parser()
                    .verifyWith(jwtPublicKey)
                    .build()
                    .parseSignedClaims(token)
                    .getPayload();

            // Check blocklist
            String jti = claims.getId();
            if (jti != null && tokenBlocklist.isBlocked(jti)) {
                return writeErrorResponse(exchange, HttpStatus.UNAUTHORIZED, "TOKEN_INVALID");
            }

            // Extract claims and add forwarded headers
            ServerHttpRequest mutatedRequest = buildMutatedRequest(exchange.getRequest(), claims);
            return chain.filter(exchange.mutate().request(mutatedRequest).build());

        } catch (ExpiredJwtException e) {
            log.debug("JWT expired: {}", e.getMessage());
            return writeErrorResponse(exchange, HttpStatus.UNAUTHORIZED, "TOKEN_EXPIRED");
        } catch (JwtException | IllegalArgumentException e) {
            log.debug("JWT invalid: {}", e.getMessage());
            return writeErrorResponse(exchange, HttpStatus.UNAUTHORIZED, "TOKEN_INVALID");
        }
    }

    private boolean isPublicPath(String path) {
        return PUBLIC_PATHS.stream().anyMatch(path::startsWith)
                || path.endsWith("/v3/api-docs")
                || path.startsWith("/swagger-ui")
                || path.startsWith("/webjars/");
    }

    @SuppressWarnings("unchecked")
    private ServerHttpRequest buildMutatedRequest(ServerHttpRequest request, Claims claims) {
        String userId = claims.getSubject();
        String entityId = claims.get("entity_id", String.class);
        String profileId = claims.get("profile_id", String.class);

        // Encode rights map as base64 JSON
        Object rights = claims.get("rights");
        String rightsBase64 = "";
        if (rights != null) {
            try {
                String rightsJson = objectMapper.writeValueAsString(rights);
                rightsBase64 = Base64.getEncoder().encodeToString(rightsJson.getBytes(StandardCharsets.UTF_8));
            } catch (JsonProcessingException e) {
                log.warn("Failed to serialize rights claim: {}", e.getMessage());
            }
        }

        return request.mutate()
                .header("X-User-Id", userId != null ? userId : "")
                .header("X-Entity-Id", entityId != null ? entityId : "")
                .header("X-Profile-Id", profileId != null ? profileId : "")
                .header("X-User-Rights", rightsBase64)
                .build();
    }

    private Mono<Void> writeErrorResponse(ServerWebExchange exchange, HttpStatus status, String errorCode) {
        ServerHttpResponse response = exchange.getResponse();
        response.setStatusCode(status);
        response.getHeaders().setContentType(MediaType.APPLICATION_JSON);

        String body = "{\"errorCode\":\"" + errorCode + "\"}";
        DataBuffer buffer = response.bufferFactory()
                .wrap(body.getBytes(StandardCharsets.UTF_8));
        return response.writeWith(Mono.just(buffer));
    }
}
