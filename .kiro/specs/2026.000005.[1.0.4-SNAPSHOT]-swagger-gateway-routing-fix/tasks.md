# Implementation Plan

- [x] 1. Write bug condition exploration test
  - **Property 1: Bug Condition** — Swagger URLs Resolve to Internal Docker Hostnames
  - **CRITICAL**: This test MUST FAIL on unfixed code — failure confirms the bug exists
  - **DO NOT attempt to fix the test or the code when it fails**
  - **NOTE**: This test encodes the expected behavior — it will validate the fix when it passes after implementation
  - **GOAL**: Surface counterexamples that demonstrate the bug exists
  - **Scoped PBT Approach**: For each of the 8 downstream services, the property asserts that the configured `springdoc.swaggerui.urls` URL is a gateway-relative path (starts with `/`) and that a matching gateway route with `StripPrefix=1` exists, and that the path is exempt from JWT authentication
  - Create test class `api-gateway/src/test/java/com/glpi/gateway/swagger/SwaggerRoutingBugConditionProperties.java`
  - Use jqwik `@Property` with `@ForAll` generating service names from the set {identity-service, ticket-service, problem-service, change-service, asset-service, sla-service, notification-service, knowledge-service}
  - Parse `application.yml` and assert: for each service, `springdoc.swaggerui.urls[service].url` starts with `/` (is a relative path, not an absolute URL with a hostname)
  - Assert: a gateway route exists with predicate `Path=/{service-name}/v3/api-docs` and filter `StripPrefix=1`
  - Assert: `JwtAuthenticationFilter.isPublicPath("/{service-name}/v3/api-docs")` returns `true`
  - Run test on UNFIXED code
  - **EXPECTED OUTCOME**: Test FAILS (this is correct — it proves the bug exists: URLs are absolute, no proxy routes exist, JWT blocks doc paths)
  - Document counterexamples found (e.g., `identity-service` URL is `http://identity-service:8081/v3/api-docs` instead of `/identity-service/v3/api-docs`)
  - Mark task complete when test is written, run, and failure is documented
  - _Requirements: 1.1, 1.2, 1.3, 1.4_

- [x] 2. Write preservation property tests (BEFORE implementing fix)
  - **Property 2: Preservation** — Existing API Routing and JWT Authentication Unchanged
  - **IMPORTANT**: Follow observation-first methodology
  - Create test class `api-gateway/src/test/java/com/glpi/gateway/swagger/SwaggerRoutingPreservationProperties.java`
  - Observe on UNFIXED code: `JwtAuthenticationFilter.isPublicPath("/auth/login")` returns `true`
  - Observe on UNFIXED code: `JwtAuthenticationFilter.isPublicPath("/auth/refresh")` returns `true`
  - Observe on UNFIXED code: `JwtAuthenticationFilter.isPublicPath("/tickets/123")` returns `false`
  - Observe on UNFIXED code: `JwtAuthenticationFilter.isPublicPath("/assets/456")` returns `false`
  - Write jqwik `@Property`: for all non-doc API paths generated from existing route prefixes (`/auth/`, `/users/`, `/tickets/`, `/problems/`, `/changes/`, `/assets/`, `/slas/`, `/olas/`, `/calendars/`, `/notifications/`, `/knowledge/`, `/entities/`, `/profiles/`, `/groups/`), `isPublicPath()` returns the same result as observed on unfixed code (only `/auth/login` and `/auth/refresh` are public)
  - Write jqwik `@Property`: for all randomly generated paths that do NOT end with `/v3/api-docs`, the JWT filter behavior is unchanged — protected paths remain protected, public paths remain public
  - Write assertion: the gateway's own `/v3/api-docs` endpoint configuration is unchanged (springdoc.api-docs.path remains `/v3/api-docs`)
  - Run tests on UNFIXED code
  - **EXPECTED OUTCOME**: Tests PASS (this confirms baseline behavior to preserve)
  - Mark task complete when tests are written, run, and passing on unfixed code
  - _Requirements: 3.1, 3.2, 3.3, 3.4_

- [x] 3. Fix Swagger gateway routing for Docker Compose

  - [x] 3.1 Add 8 gateway proxy routes for downstream OpenAPI specs in `application.yml`
    - Add routes BEFORE existing service routes for defensive ordering
    - Each route: `id: {service}-docs`, `uri: ${SERVICE_URL:http://localhost:PORT}`, `predicates: Path=/{service-name}/v3/api-docs`, `filters: StripPrefix=1`
    - Services: identity (8081), ticket (8082), problem (8083), change (8084), asset (8085), sla (8086), notification (8087), knowledge (8088)
    - _Bug_Condition: isBugCondition(X) where X.swaggerUrlHost ≠ "localhost" AND X.swaggerUrlHost ≠ "127.0.0.1"_
    - _Expected_Behavior: gateway proxies /{service-name}/v3/api-docs to downstream service's /v3/api-docs via StripPrefix=1_
    - _Preservation: existing routes (/auth/**, /users/**, /tickets/**, etc.) remain unchanged_
    - _Requirements: 2.2, 2.4_

  - [x] 3.2 Rewrite `springdoc.swaggerui.urls` to use gateway-relative paths in `application.yml`
    - Replace each absolute URL (e.g., `${IDENTITY_SERVICE_URL:http://localhost:8081}/v3/api-docs`) with the gateway-relative path (e.g., `/identity-service/v3/api-docs`)
    - Apply to all 8 services
    - _Bug_Condition: configuredUrl is absolute with internal Docker hostname_
    - _Expected_Behavior: configuredUrl is a relative path starting with "/"_
    - _Preservation: service names in the dropdown remain unchanged_
    - _Requirements: 2.1, 2.3_

  - [x] 3.3 Update `JwtAuthenticationFilter` to exempt doc and Swagger UI paths from JWT validation
    - In `JwtAuthenticationFilter.java`, add to `PUBLIC_PATHS` or modify `isPublicPath()` to also match:
      - Any path ending with `/v3/api-docs` (covers gateway own spec + all 8 proxied specs)
      - `/swagger-ui.html`
      - `/swagger-ui/` (prefix match)
      - `/webjars/` (prefix match)
    - Cleanest approach: modify `isPublicPath()` to check `path.endsWith("/v3/api-docs")` OR `path.startsWith("/swagger-ui")` OR `path.startsWith("/webjars/")` in addition to existing `PUBLIC_PATHS` check
    - _Bug_Condition: JWT filter blocks unauthenticated requests to /{service-name}/v3/api-docs_
    - _Expected_Behavior: isPublicPath returns true for all doc and Swagger UI paths_
    - _Preservation: isPublicPath returns same result for all non-doc, non-swagger paths_
    - _Requirements: 2.2, 3.2, 3.4_

  - [x] 3.4 Verify bug condition exploration test now passes
    - **Property 1: Expected Behavior** — Swagger URLs Use Gateway-Relative Paths
    - **IMPORTANT**: Re-run the SAME test from task 1 — do NOT write a new test
    - The test from task 1 encodes the expected behavior (relative paths, proxy routes, public JWT paths)
    - When this test passes, it confirms the expected behavior is satisfied
    - Run bug condition exploration test from step 1
    - **EXPECTED OUTCOME**: Test PASSES (confirms bug is fixed)
    - _Requirements: 2.1, 2.2, 2.3, 2.4_

  - [x] 3.5 Verify preservation tests still pass
    - **Property 2: Preservation** — Existing API Routing and JWT Authentication Unchanged
    - **IMPORTANT**: Re-run the SAME tests from task 2 — do NOT write new tests
    - Run preservation property tests from step 2
    - **EXPECTED OUTCOME**: Tests PASS (confirms no regressions)
    - Confirm all tests still pass after fix (no regressions)

- [x] 4. Checkpoint — Ensure all tests pass
  - Run full test suite for api-gateway module: `mvn -pl api-gateway test`
  - Ensure all property-based tests (bug condition + preservation) pass
  - Ensure no compilation errors or warnings
  - Ask the user if questions arise

- [x] 5. Version control and release
  - [x] 5.1 Ensure all previous tasks are complete and tests pass
  - [x] 5.2 Remove SNAPSHOT suffix from all version references in the codebase
  - [x] 5.3 Commit the version bump: "release: 1.0.4 - swagger-gateway-routing-fix"
  - [x] 5.4 Merge branch into main/master
  - [x] 5.5 Apply Git tag: 1.0.4 (without SNAPSHOT)
  - [x] 5.6 Push branch, merge, and tag to remote
