package com.glpi.gateway.swagger;

import net.jqwik.api.*;
import org.yaml.snakeyaml.Yaml;

import java.io.InputStream;
import java.lang.reflect.Method;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.*;

/**
 * Bug Condition Exploration Test — Swagger URLs Resolve to Internal Docker Hostnames.
 *
 * <p>This property test verifies the EXPECTED behavior: swagger URLs should be
 * gateway-relative paths, proxy routes should exist, and JWT should allow doc paths.
 * On UNFIXED code, this test is EXPECTED TO FAIL, confirming the bug exists.</p>
 *
 * <p><b>Validates: Requirements 1.1, 1.2, 1.3, 1.4</b></p>
 */
class SwaggerRoutingBugConditionProperties {

    private static final List<String> SERVICE_NAMES = List.of(
            "identity-service",
            "ticket-service",
            "problem-service",
            "change-service",
            "asset-service",
            "sla-service",
            "notification-service",
            "knowledge-service"
    );

    @SuppressWarnings("unchecked")
    private Map<String, Object> loadApplicationYaml() {
        Yaml yaml = new Yaml();
        Path yamlPath = Path.of("src/main/resources/application.yml");
        try (InputStream is = Files.newInputStream(yamlPath)) {
            return yaml.load(is);
        } catch (Exception e) {
            throw new RuntimeException("Failed to load application.yml from " + yamlPath.toAbsolutePath(), e);
        }
    }

    @Provide
    Arbitrary<String> serviceNames() {
        return Arbitraries.of(SERVICE_NAMES);
    }

    /**
     * Property 1: Bug Condition — Swagger spec URLs must be gateway-relative paths.
     *
     * <p>For each downstream service, the configured {@code springdoc.swaggerui.urls} URL
     * must start with "/" (a relative path), not be an absolute URL with a hostname.</p>
     *
     * <p><b>Validates: Requirements 1.1, 1.2</b></p>
     */
    @Property
    @Label("Swagger URLs are gateway-relative paths (not absolute URLs with hostnames)")
    void swaggerUrlsMustBeRelativePaths(@ForAll("serviceNames") String serviceName) {
        Map<String, Object> config = loadApplicationYaml();
        String url = findSwaggerUrlForService(config, serviceName);

        assert url != null : "No swagger URL configured for service: " + serviceName;
        assert url.startsWith("/") :
                "Swagger URL for " + serviceName + " is not a relative path: " + url +
                " (expected: /" + serviceName + "/v3/api-docs)";
    }

    /**
     * Property 1: Bug Condition — Gateway proxy routes must exist for doc endpoints.
     *
     * <p>For each downstream service, a gateway route must exist with predicate
     * {@code Path=/{service-name}/v3/api-docs} and filter {@code StripPrefix=1}.</p>
     *
     * <p><b>Validates: Requirements 1.3, 1.4</b></p>
     */
    @Property
    @Label("Gateway proxy routes exist for /{service}/v3/api-docs with StripPrefix=1")
    void gatewayDocRoutesMustExist(@ForAll("serviceNames") String serviceName) {
        Map<String, Object> config = loadApplicationYaml();
        String expectedPath = "/" + serviceName + "/v3/api-docs";

        List<Map<String, Object>> routes = getGatewayRoutes(config);
        boolean routeFound = false;
        boolean stripPrefixFound = false;

        for (Map<String, Object> route : routes) {
            List<String> predicates = getStringList(route, "predicates");
            if (predicates.stream().anyMatch(p -> p.contains("Path=" + expectedPath))) {
                routeFound = true;
                List<String> filters = getStringList(route, "filters");
                stripPrefixFound = filters.stream().anyMatch(f -> f.contains("StripPrefix=1"));
                break;
            }
        }

        assert routeFound :
                "No gateway route found with predicate Path=" + expectedPath;
        assert stripPrefixFound :
                "Gateway route for " + expectedPath + " is missing StripPrefix=1 filter";
    }

    /**
     * Property 1: Bug Condition — JWT filter must exempt doc paths from authentication.
     *
     * <p>For each downstream service, {@code isPublicPath("/{service-name}/v3/api-docs")}
     * must return {@code true}.</p>
     *
     * <p><b>Validates: Requirements 1.3, 1.4</b></p>
     */
    @Property
    @Label("JWT filter exempts /{service}/v3/api-docs from authentication")
    void jwtFilterMustExemptDocPaths(@ForAll("serviceNames") String serviceName) {
        String docPath = "/" + serviceName + "/v3/api-docs";
        boolean isPublic = invokeIsPublicPath(docPath);

        assert isPublic :
                "JwtAuthenticationFilter.isPublicPath(\"" + docPath + "\") returned false — " +
                "doc paths are blocked by JWT authentication";
    }

    // --- Helper methods ---

    @SuppressWarnings("unchecked")
    private String findSwaggerUrlForService(Map<String, Object> config, String serviceName) {
        Map<String, Object> springdoc = (Map<String, Object>) config.get("springdoc");
        if (springdoc == null) return null;

        Map<String, Object> swaggerui = (Map<String, Object>) springdoc.get("swaggerui");
        if (swaggerui == null) return null;

        List<Map<String, Object>> urls = (List<Map<String, Object>>) swaggerui.get("urls");
        if (urls == null) return null;

        for (Map<String, Object> entry : urls) {
            if (serviceName.equals(entry.get("name"))) {
                return (String) entry.get("url");
            }
        }
        return null;
    }

    @SuppressWarnings("unchecked")
    private List<Map<String, Object>> getGatewayRoutes(Map<String, Object> config) {
        Map<String, Object> spring = (Map<String, Object>) config.get("spring");
        if (spring == null) return Collections.emptyList();

        Map<String, Object> cloud = (Map<String, Object>) spring.get("cloud");
        if (cloud == null) return Collections.emptyList();

        Map<String, Object> gateway = (Map<String, Object>) cloud.get("gateway");
        if (gateway == null) return Collections.emptyList();

        List<Map<String, Object>> routes = (List<Map<String, Object>>) gateway.get("routes");
        return routes != null ? routes : Collections.emptyList();
    }

    @SuppressWarnings("unchecked")
    private List<String> getStringList(Map<String, Object> map, String key) {
        Object value = map.get(key);
        if (value instanceof List) {
            return ((List<?>) value).stream()
                    .map(Object::toString)
                    .toList();
        }
        return Collections.emptyList();
    }

    /**
     * Invokes the private {@code isPublicPath} method on a new instance of
     * {@code JwtAuthenticationFilter} using reflection. Since the filter's constructor
     * requires dependencies, we access the method's logic by reading the PUBLIC_PATHS
     * constant directly and replicating the check.
     */
    private boolean invokeIsPublicPath(String path) {
        try {
            // Access the PUBLIC_PATHS field via reflection
            Class<?> filterClass = Class.forName(
                    "com.glpi.gateway.filter.JwtAuthenticationFilter");
            var field = filterClass.getDeclaredField("PUBLIC_PATHS");
            field.setAccessible(true);
            @SuppressWarnings("unchecked")
            List<String> publicPaths = (List<String>) field.get(null);

            // Replicate the isPublicPath logic: path.startsWith(publicPath)
            return publicPaths.stream().anyMatch(path::startsWith);
        } catch (Exception e) {
            throw new RuntimeException("Failed to access JwtAuthenticationFilter.PUBLIC_PATHS", e);
        }
    }
}
