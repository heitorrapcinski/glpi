# Swagger Gateway Routing Fix — Bugfix Design

## Overview

The API Gateway's Swagger UI fails in Docker Compose because `springdoc.swaggerui.urls` resolves to internal Docker hostnames (e.g., `http://asset-service:8085/v3/api-docs`) that the browser cannot reach. The fix adds gateway proxy routes for each downstream service's `/v3/api-docs` endpoint, rewrites the swagger URL configuration to use gateway-relative paths, and exempts those paths from JWT authentication. This is a configuration-and-filter change — no business logic is modified.

## Glossary

- **Bug_Condition (C)**: Swagger UI attempts to fetch an OpenAPI spec from an internal Docker hostname that the browser cannot resolve
- **Property (P)**: Swagger UI fetches all OpenAPI specs through gateway-relative paths (`/{service}/v3/api-docs`) that the gateway proxies to the correct downstream service, returning HTTP 200 with a valid spec and no CORS errors
- **Preservation**: All existing API routing (`/tickets/**`, `/assets/**`, etc.), JWT validation behavior, local development defaults, and the gateway's own `/v3/api-docs` endpoint remain unchanged
- **`application.yml`**: Spring Cloud Gateway configuration in `api-gateway/src/main/resources/application.yml` — defines routes, springdoc URLs, and service URIs
- **`JwtAuthenticationFilter`**: Global filter in `api-gateway/src/main/java/com/glpi/gateway/filter/JwtAuthenticationFilter.java` — validates JWT tokens; maintains a `PUBLIC_PATHS` list of endpoints that bypass authentication
- **`StripPrefix`**: Spring Cloud Gateway filter that removes N path segments before forwarding; `StripPrefix=1` on `/identity-service/v3/api-docs` forwards as `/v3/api-docs`
- **Downstream services**: identity, ticket, problem, change, asset, sla, notification, knowledge (8 total)

## Bug Details

### Bug Condition

The bug manifests when the application runs inside Docker Compose and a user opens Swagger UI at `http://localhost:8080/swagger-ui.html`. The `springdoc.swaggerui.urls` entries resolve environment variables to internal Docker service hostnames (e.g., `http://asset-service:8085/v3/api-docs`). The browser, running outside the Docker network, cannot resolve these hostnames, causing DNS failures and CORS errors ("URL origin does not match the page").

**Formal Specification:**
```
FUNCTION isBugCondition(input)
  INPUT: input of type SwaggerSpecRequest
  OUTPUT: boolean

  // Extract the configured URL for the requested service spec
  LET configuredUrl = springdoc.swaggerui.urls[input.serviceName].url

  // The bug triggers when the URL contains a non-browser-resolvable host
  RETURN NOT isRelativePath(configuredUrl)
         AND extractHost(configuredUrl) NOT IN ["localhost", "127.0.0.1"]
END FUNCTION
```

### Examples

- **Identity Service in Docker**: Swagger UI tries to fetch `http://identity-service:8081/v3/api-docs` → browser DNS failure. Expected: fetch `/identity-service/v3/api-docs` → gateway proxies to downstream → HTTP 200 with valid spec.
- **Asset Service in Docker**: Swagger UI tries to fetch `http://asset-service:8085/v3/api-docs` → CORS error (origin mismatch). Expected: fetch `/asset-service/v3/api-docs` → same-origin request → no CORS error.
- **Local development (no Docker)**: Swagger UI fetches `http://localhost:8085/v3/api-docs` → works because browser can resolve localhost. After fix: fetches `/asset-service/v3/api-docs` → gateway proxies to `http://localhost:8085/v3/api-docs` → still works.
- **Edge case — gateway's own spec**: Request to `/v3/api-docs` must still return the gateway's own OpenAPI spec, not be intercepted by the new proxy routes.

## Expected Behavior

### Preservation Requirements

**Unchanged Behaviors:**
- All existing API routes (`/auth/**`, `/users/**`, `/tickets/**`, `/problems/**`, `/changes/**`, `/assets/**`, `/slas/**`, `/olas/**`, `/calendars/**`, `/notifications/**`, `/knowledge/**`) continue to proxy to the correct downstream services with identical behavior
- JWT validation continues to enforce authentication on all non-public endpoints
- The gateway's own `/v3/api-docs` endpoint continues to return the gateway's OpenAPI spec
- Local development without Docker (using default `localhost` URLs) continues to work — the gateway-relative paths proxy through the same routes
- Mouse/keyboard interactions with Swagger UI (try-it-out, model expansion, etc.) remain unaffected
- The `gateway.downstream-services` configuration block remains unchanged
- CORS global configuration remains unchanged

**Scope:**
All inputs that do NOT involve fetching downstream OpenAPI specs through Swagger UI should be completely unaffected by this fix. This includes:
- All REST API calls to business endpoints
- Health/actuator endpoints
- JWT token validation and header forwarding
- Rate limiting behavior

## Hypothesized Root Cause

Based on the bug description and code analysis, the root cause is a configuration issue — not a code defect:

1. **Absolute URLs in `springdoc.swaggerui.urls`**: The `application.yml` configures each swagger URL as `${SERVICE_URL}/v3/api-docs`. In Docker Compose, these environment variables resolve to internal hostnames (e.g., `http://asset-service:8085`). Swagger UI runs in the browser and emits `fetch()` calls to these absolute URLs, which the browser cannot resolve.

2. **No gateway proxy routes for `/v3/api-docs`**: The gateway has no routes that match `/{service-name}/v3/api-docs`, so even if the URLs were changed to relative paths, there would be no route to forward the request to the downstream service.

3. **JWT filter blocks unauthenticated doc requests**: The `JwtAuthenticationFilter.PUBLIC_PATHS` only contains `/auth/login` and `/auth/refresh`. Any new `/{service-name}/v3/api-docs` routes would be blocked by JWT validation unless explicitly added to the public paths list. Swagger UI does not send JWT tokens when fetching specs.

4. **No `StripPrefix` filter**: Without a `StripPrefix=1` filter on the new routes, the gateway would forward the full path (e.g., `/identity-service/v3/api-docs`) to the downstream service, which only listens on `/v3/api-docs`.

## Correctness Properties

Property 1: Bug Condition — Swagger Spec Requests Use Gateway-Relative Paths

_For any_ downstream service in the set {identity, ticket, problem, change, asset, sla, notification, knowledge}, the `springdoc.swaggerui.urls` configuration SHALL specify a gateway-relative path (`/{service-name}/v3/api-docs`) instead of an absolute URL containing a hostname, AND the gateway SHALL have a matching route that proxies the request to the downstream service's `/v3/api-docs` endpoint using `StripPrefix=1`, AND the path SHALL be exempt from JWT authentication.

**Validates: Requirements 2.1, 2.2, 2.3, 2.4**

Property 2: Preservation — Existing API Routing and Authentication Unchanged

_For any_ request that does NOT target a `/{service-name}/v3/api-docs` path, the fixed gateway SHALL produce exactly the same routing, JWT validation, and response behavior as the original gateway, preserving all existing API routes, authentication enforcement, and the gateway's own `/v3/api-docs` endpoint.

**Validates: Requirements 3.1, 3.2, 3.3, 3.4**

## Fix Implementation

### Changes Required

Assuming our root cause analysis is correct:

**File**: `api-gateway/src/main/resources/application.yml`

**Section**: `spring.cloud.gateway.routes`

**Specific Changes**:
1. **Add 8 proxy routes for downstream OpenAPI specs**: Add a route for each downstream service that matches `/{service-name}/v3/api-docs` and forwards to the corresponding service URL. Each route uses `StripPrefix=1` to strip the service-name prefix before forwarding.
   - Route: `identity-service-docs` → `Path=/identity-service/v3/api-docs` → `uri: ${IDENTITY_SERVICE_URL:http://localhost:8081}` → `StripPrefix=1`
   - Route: `ticket-service-docs` → `Path=/ticket-service/v3/api-docs` → `uri: ${TICKET_SERVICE_URL:http://localhost:8082}` → `StripPrefix=1`
   - Route: `problem-service-docs` → `Path=/problem-service/v3/api-docs` → `uri: ${PROBLEM_SERVICE_URL:http://localhost:8083}` → `StripPrefix=1`
   - Route: `change-service-docs` → `Path=/change-service/v3/api-docs` → `uri: ${CHANGE_SERVICE_URL:http://localhost:8084}` → `StripPrefix=1`
   - Route: `asset-service-docs` → `Path=/asset-service/v3/api-docs` → `uri: ${ASSET_SERVICE_URL:http://localhost:8085}` → `StripPrefix=1`
   - Route: `sla-service-docs` → `Path=/sla-service/v3/api-docs` → `uri: ${SLA_SERVICE_URL:http://localhost:8086}` → `StripPrefix=1`
   - Route: `notification-service-docs` → `Path=/notification-service/v3/api-docs` → `uri: ${NOTIFICATION_SERVICE_URL:http://localhost:8087}` → `StripPrefix=1`
   - Route: `knowledge-service-docs` → `Path=/knowledge-service/v3/api-docs` → `uri: ${KNOWLEDGE_SERVICE_URL:http://localhost:8088}` → `StripPrefix=1`

2. **Rewrite `springdoc.swaggerui.urls` to use relative paths**: Replace each absolute URL with the corresponding gateway-relative path.
   - Before: `url: ${IDENTITY_SERVICE_URL:http://localhost:8081}/v3/api-docs`
   - After: `url: /identity-service/v3/api-docs`
   - (Repeat for all 8 services)

**File**: `api-gateway/src/main/java/com/glpi/gateway/filter/JwtAuthenticationFilter.java`

**Constant**: `PUBLIC_PATHS`

**Specific Changes**:
3. **Add swagger doc proxy paths to `PUBLIC_PATHS`**: Add a single path prefix entry that covers all 8 service doc routes. Since all paths end with `/v3/api-docs`, we can add individual entries or use a pattern. The simplest approach: add each `/{service-name}/v3/api-docs` path, or add a single check for paths ending in `/v3/api-docs`.
   - Recommended: Add a single entry `/v3/api-docs` suffix check, or add the Swagger UI paths (`/swagger-ui`, `/webjars`) and all 8 service doc paths to `PUBLIC_PATHS`.
   - Cleanest approach: Modify `isPublicPath()` to also match any path ending with `/v3/api-docs`, covering both the gateway's own spec and all proxied specs.

4. **Also ensure Swagger UI static resources are public**: The paths `/swagger-ui.html`, `/swagger-ui/**`, and `/webjars/**` should also be in `PUBLIC_PATHS` if not already handled by Spring Security (they are currently permitted by `SecurityConfig` via `permitAll()`, but the `JwtAuthenticationFilter` runs as a `GlobalFilter` at order -200, before Spring Security). Add these paths to prevent JWT errors when loading Swagger UI.

5. **Route ordering**: Place the new doc routes BEFORE the existing service routes in `application.yml` to ensure `/identity-service/v3/api-docs` is matched by the doc route, not by a broader pattern. Since existing routes use paths like `/auth/**` and `/users/**` (not `/identity-service/**`), there is no conflict — but placing doc routes first is a defensive best practice.

## Testing Strategy

### Validation Approach

The testing strategy follows a two-phase approach: first, surface counterexamples that demonstrate the bug on unfixed code, then verify the fix works correctly and preserves existing behavior.

### Exploratory Bug Condition Checking

**Goal**: Surface counterexamples that demonstrate the bug BEFORE implementing the fix. Confirm or refute the root cause analysis. If we refute, we will need to re-hypothesize.

**Test Plan**: Inspect the `application.yml` configuration and `JwtAuthenticationFilter` to verify that (a) swagger URLs contain absolute internal hostnames, (b) no proxy routes exist for `/{service-name}/v3/api-docs`, and (c) the JWT filter would block unauthenticated requests to those paths. Run a Spring context load test to confirm route resolution behavior.

**Test Cases**:
1. **Config Inspection Test**: Parse `application.yml` and assert that `springdoc.swaggerui.urls[*].url` contains absolute URLs with non-localhost hostnames when Docker env vars are set (will fail on unfixed code — confirms bug condition)
2. **Missing Route Test**: Attempt to resolve a route for `/identity-service/v3/api-docs` and assert it returns 404 (will fail on unfixed code — confirms no proxy route exists)
3. **JWT Block Test**: Send an unauthenticated GET to `/identity-service/v3/api-docs` and assert it returns 401 (will fail on unfixed code — confirms JWT filter blocks the path)
4. **Gateway Own Spec Test**: Send GET to `/v3/api-docs` and assert it returns the gateway's own spec (baseline — should pass on both unfixed and fixed code)

**Expected Counterexamples**:
- Swagger URLs resolve to `http://identity-service:8081/v3/api-docs` instead of `/identity-service/v3/api-docs`
- No route matches `/{service-name}/v3/api-docs` — gateway returns 404
- JWT filter returns 401 for unauthenticated doc requests

### Fix Checking

**Goal**: Verify that for all inputs where the bug condition holds, the fixed function produces the expected behavior.

**Pseudocode:**
```
FOR ALL service IN {identity, ticket, problem, change, asset, sla, notification, knowledge} DO
  // Verify config uses relative path
  configUrl := springdoc.swaggerui.urls[service].url
  ASSERT configUrl = "/" + service + "-service/v3/api-docs"

  // Verify route exists and proxies correctly
  result := gateway.route("GET", "/" + service + "-service/v3/api-docs")
  ASSERT result.status = 200
  ASSERT result.body IS valid_openapi_json

  // Verify JWT filter allows unauthenticated access
  ASSERT jwtFilter.isPublicPath("/" + service + "-service/v3/api-docs") = true
END FOR
```

### Preservation Checking

**Goal**: Verify that for all inputs where the bug condition does NOT hold, the fixed function produces the same result as the original function.

**Pseudocode:**
```
FOR ALL request WHERE NOT request.path MATCHES "/{service}-service/v3/api-docs" DO
  ASSERT gateway_original.route(request) = gateway_fixed.route(request)
  ASSERT jwtFilter_original.isPublicPath(request.path) = jwtFilter_fixed.isPublicPath(request.path)
END FOR
```

**Testing Approach**: Property-based testing is recommended for preservation checking because:
- It generates many request paths automatically across the input domain
- It catches edge cases where new routes might accidentally shadow existing routes
- It provides strong guarantees that JWT filter behavior is unchanged for all non-doc paths

**Test Plan**: Capture the routing and JWT filter behavior on UNFIXED code for a set of existing API paths, then write property-based tests verifying that the FIXED code produces identical results for those paths.

**Test Cases**:
1. **API Route Preservation**: Verify that requests to `/tickets/**`, `/assets/**`, `/auth/**`, etc. continue to route to the same downstream services with the same URI transformations
2. **JWT Filter Preservation**: Verify that `isPublicPath()` returns the same result for all non-doc paths (e.g., `/tickets/123` → false, `/auth/login` → true)
3. **Gateway Own Spec Preservation**: Verify that GET `/v3/api-docs` still returns the gateway's own OpenAPI spec, not a downstream service spec
4. **CORS Config Preservation**: Verify that the global CORS configuration is unchanged

### Unit Tests

- Test `JwtAuthenticationFilter.isPublicPath()` with doc paths → returns true
- Test `JwtAuthenticationFilter.isPublicPath()` with existing public paths → still returns true
- Test `JwtAuthenticationFilter.isPublicPath()` with protected paths → still returns false
- Test route configuration parsing — verify 8 doc routes exist with correct predicates and filters

### Property-Based Tests

- Generate random service names from the set of 8 services and verify each has a working doc proxy route
- Generate random non-doc API paths and verify JWT filter behavior is unchanged
- Generate random request paths and verify no route shadowing occurs between doc routes and existing API routes

### Integration Tests

- Start gateway with Docker-like environment variables and verify Swagger UI can load all 8 specs through gateway-relative paths
- Start gateway with default localhost URLs and verify Swagger UI still works (local dev preservation)
- Verify full Swagger UI page load — dropdown shows all 8 services, selecting each loads a valid spec
