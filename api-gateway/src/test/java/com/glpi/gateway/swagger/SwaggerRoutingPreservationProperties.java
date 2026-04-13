package com.glpi.gateway.swagger;

import net.jqwik.api.*;
import org.yaml.snakeyaml.Yaml;

import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.*;

/**
 * Preservation Property Tests — Existing API Routing and JWT Authentication Unchanged.
 *
 * <p>These tests capture the baseline behavior of the UNFIXED code and verify that
 * the fix does not introduce regressions. They MUST PASS on both unfixed and fixed code.</p>
 *
 * <p><b>Validates: Requirements 3.1, 3.2, 3.3, 3.4</b></p>
 */
class SwaggerRoutingPreservationProperties {

    /**
     * Existing route prefixes from application.yml that the gateway proxies to downstream services.
     */
    private static final List<String> EXISTING_ROUTE_PREFIXES = List.of(
            "/auth/",
            "/users/",
            "/tickets/",
            "/problems/",
            "/changes/",
            "/assets/",
            "/slas/",
            "/olas/",
            "/calendars/",
            "/notifications/",
            "/knowledge/",
            "/entities/",
            "/profiles/",
            "/groups/"
    );

    /**
     * Observed baseline: only these paths are public on UNFIXED code.
     * JwtAuthenticationFilter.isPublicPath uses startsWith matching against PUBLIC_PATHS.
     */
    private static final List<String> OBSERVED_PUBLIC_PREFIXES = List.of(
            "/auth/login",
            "/auth/refresh"
    );

    // --- Observation tests (confirm baseline on UNFIXED code) ---

    /**
     * Observation: /auth/login is a public path on unfixed code.
     */
    @Example
    @Label("Observe: /auth/login is public on unfixed code")
    void observeAuthLoginIsPublic() {
        assert invokeIsPublicPath("/auth/login") :
                "Expected /auth/login to be public on unfixed code";
    }

    /**
     * Observation: /auth/refresh is a public path on unfixed code.
     */
    @Example
    @Label("Observe: /auth/refresh is public on unfixed code")
    void observeAuthRefreshIsPublic() {
        assert invokeIsPublicPath("/auth/refresh") :
                "Expected /auth/refresh to be public on unfixed code";
    }

    /**
     * Observation: /tickets/123 is a protected path on unfixed code.
     */
    @Example
    @Label("Observe: /tickets/123 is protected on unfixed code")
    void observeTicketsIsProtected() {
        assert !invokeIsPublicPath("/tickets/123") :
                "Expected /tickets/123 to be protected on unfixed code";
    }

    /**
     * Observation: /assets/456 is a protected path on unfixed code.
     */
    @Example
    @Label("Observe: /assets/456 is protected on unfixed code")
    void observeAssetsIsProtected() {
        assert !invokeIsPublicPath("/assets/456") :
                "Expected /assets/456 to be protected on unfixed code";
    }

    // --- Providers ---

    @Provide
    Arbitrary<String> existingRoutePrefixes() {
        return Arbitraries.of(EXISTING_ROUTE_PREFIXES);
    }

    @Provide
    Arbitrary<String> pathSuffixes() {
        return Arbitraries.strings()
                .alpha()
                .ofMinLength(1)
                .ofMaxLength(20);
    }

    @Provide
    Arbitrary<String> nonDocApiPaths() {
        // Generate paths from existing route prefixes with random suffixes
        // that do NOT end with /v3/api-docs
        return Combinators.combine(
                existingRoutePrefixes(),
                pathSuffixes()
        ).as((prefix, suffix) -> prefix + suffix);
    }

    @Provide
    Arbitrary<String> randomNonDocPaths() {
        // Generate completely random paths that do NOT end with /v3/api-docs
        return Arbitraries.strings()
                .alpha()
                .ofMinLength(1)
                .ofMaxLength(30)
                .map(s -> "/" + s)
                .filter(p -> !p.endsWith("/v3/api-docs"));
    }

    // --- Property tests ---

    /**
     * Property 2: Preservation — For all non-doc API paths generated from existing route
     * prefixes, isPublicPath() returns the same result as observed on unfixed code.
     *
     * <p>On unfixed code, only paths starting with "/auth/login" or "/auth/refresh" are public.
     * All other existing route prefix paths are protected.</p>
     *
     * <p><b>Validates: Requirements 3.1, 3.2</b></p>
     */
    @Property(tries = 200)
    @Label("Existing route prefix paths: isPublicPath matches observed baseline")
    void existingRoutePathsPreserveJwtBehavior(@ForAll("nonDocApiPaths") String path) {
        boolean actual = invokeIsPublicPath(path);
        boolean expected = computeExpectedPublicPath(path);

        assert actual == expected :
                "isPublicPath(\"" + path + "\") returned " + actual +
                " but expected " + expected + " based on observed baseline";
    }

    /**
     * Property 2: Preservation — For all randomly generated paths that do NOT end with
     * /v3/api-docs, the JWT filter behavior is unchanged. Protected paths remain protected,
     * public paths remain public.
     *
     * <p><b>Validates: Requirements 3.2, 3.3</b></p>
     */
    @Property(tries = 200)
    @Label("Random non-doc paths: JWT filter behavior unchanged")
    void randomNonDocPathsPreserveJwtBehavior(@ForAll("randomNonDocPaths") String path) {
        boolean actual = invokeIsPublicPath(path);
        boolean expected = computeExpectedPublicPath(path);

        assert actual == expected :
                "isPublicPath(\"" + path + "\") returned " + actual +
                " but expected " + expected + " based on observed baseline";
    }

    /**
     * Assertion: The gateway's own /v3/api-docs endpoint configuration is unchanged.
     * springdoc.api-docs.path must remain "/v3/api-docs".
     *
     * <p><b>Validates: Requirements 3.4</b></p>
     */
    @Example
    @Label("Gateway own /v3/api-docs config is unchanged")
    void gatewayOwnApiDocsConfigUnchanged() {
        Map<String, Object> config = loadApplicationYaml();
        String apiDocsPath = getApiDocsPath(config);

        assert "/v3/api-docs".equals(apiDocsPath) :
                "springdoc.api-docs.path expected '/v3/api-docs' but was '" + apiDocsPath + "'";
    }

    // --- Helper methods ---

    /**
     * Computes the expected isPublicPath result based on observed baseline behavior.
     * On unfixed code, only paths starting with "/auth/login" or "/auth/refresh" are public.
     */
    private boolean computeExpectedPublicPath(String path) {
        return OBSERVED_PUBLIC_PREFIXES.stream().anyMatch(path::startsWith);
    }

    /**
     * Invokes the private isPublicPath logic by reading PUBLIC_PATHS via reflection
     * and replicating the full check including the additional endsWith/startsWith
     * conditions. Same approach as SwaggerRoutingBugConditionProperties.
     */
    private boolean invokeIsPublicPath(String path) {
        try {
            Class<?> filterClass = Class.forName(
                    "com.glpi.gateway.filter.JwtAuthenticationFilter");
            var field = filterClass.getDeclaredField("PUBLIC_PATHS");
            field.setAccessible(true);
            @SuppressWarnings("unchecked")
            List<String> publicPaths = (List<String>) field.get(null);

            // Replicate the full isPublicPath logic to match the method under test
            return publicPaths.stream().anyMatch(path::startsWith)
                    || path.endsWith("/v3/api-docs")
                    || path.startsWith("/swagger-ui")
                    || path.startsWith("/webjars/");
        } catch (Exception e) {
            throw new RuntimeException("Failed to access JwtAuthenticationFilter.PUBLIC_PATHS", e);
        }
    }

    @SuppressWarnings("unchecked")
    private Map<String, Object> loadApplicationYaml() {
        Yaml yaml = new Yaml();
        Path yamlPath = Path.of("api-gateway/src/main/resources/application.yml");
        try (InputStream is = Files.newInputStream(yamlPath)) {
            return yaml.load(is);
        } catch (Exception e) {
            throw new RuntimeException("Failed to load application.yml from " + yamlPath.toAbsolutePath(), e);
        }
    }

    @SuppressWarnings("unchecked")
    private String getApiDocsPath(Map<String, Object> config) {
        Map<String, Object> springdoc = (Map<String, Object>) config.get("springdoc");
        if (springdoc == null) return null;

        Map<String, Object> apiDocs = (Map<String, Object>) springdoc.get("api-docs");
        if (apiDocs == null) return null;

        return (String) apiDocs.get("path");
    }
}
