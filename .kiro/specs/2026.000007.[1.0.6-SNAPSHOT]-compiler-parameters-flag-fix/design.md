# Compiler Parameters Flag Fix — Bugfix Design

## Overview

All paginated list endpoints across every microservice fail at runtime in Docker because the `maven-compiler-plugin` in the root `pom.xml` is missing the `-parameters` compiler flag. Without this flag, Java 21 does not retain method parameter names in compiled bytecode, causing Spring Boot's `@RequestParam` resolution to fail when the `value` attribute is not explicitly specified. The fix is a single-line addition (`<parameters>true</parameters>`) to the root POM's compiler plugin configuration, which propagates to all 9 child modules.

A secondary issue exists: 7 of 9 services are missing `application-docker.yml` profile files. While `docker-compose.yml` currently injects environment variables that override defaults, the absence of these files is a latent configuration risk. The fix creates these files following the pattern established in ticket-service and notification-service (spec 2026.000006).

## Glossary

- **Bug_Condition (C)**: Any HTTP request targeting a paginated list endpoint in Docker where `@RequestParam` parameters (`page`, `size`, `sort`, `order`) lack an explicit `value` attribute and the bytecode was compiled without the `-parameters` flag
- **Property (P)**: The desired behavior — Spring resolves parameter names via reflection, the endpoint returns HTTP 200 with a valid `PagedResponse`
- **Preservation**: All non-paginated endpoints, local development, existing tests, Kafka reconnection config (spec 2026.000006), and DLQ routing in notification-service must remain unchanged
- **`maven-compiler-plugin`**: The Maven plugin in `pom.xml` (line ~165) that controls Java compilation settings for all modules
- **`-parameters` flag**: A `javac` compiler option that retains method parameter names in bytecode, enabling runtime reflection-based parameter name discovery
- **`@RequestParam(defaultValue = "0") int page`**: Spring annotation that binds a query parameter to a method argument; without `-parameters`, Spring cannot infer the name `page` from bytecode
- **`application-docker.yml`**: Spring Boot profile-specific configuration file loaded when `SPRING_PROFILES_ACTIVE=docker` is set

## Bug Details

### Bug Condition

The bug manifests when any paginated list endpoint is called in Docker. The `maven-compiler-plugin` configuration in the root `pom.xml` does not include `<parameters>true</parameters>`, so compiled bytecode does not retain method parameter names. Spring Boot's `@RequestParam` resolution fails because it cannot determine the parameter name (`page`, `size`, `sort`, `order`) via reflection when the `value` attribute is omitted.

**Formal Specification:**
```
FUNCTION isBugCondition(input)
  INPUT: input of type HttpRequest
  OUTPUT: boolean

  RETURN input.endpoint IN paginated_list_endpoints
         AND input.controllerMethod.hasRequestParamWithoutExplicitValue()
         AND compilerParametersFlag = false
END FUNCTION
```

Where `paginated_list_endpoints` includes all 17 affected endpoints:
- identity-service: `GET /users`, `GET /entities`, `GET /groups`, `GET /profiles`
- ticket-service: `GET /tickets`
- problem-service: `GET /problems`
- change-service: `GET /changes`
- asset-service: `GET /assets/{type}`, `GET /licenses`
- sla-service: `GET /slas`, `GET /olas`, `GET /calendars`
- notification-service: `GET /notifications/templates`, `GET /notifications/queue`
- knowledge-service: `GET /kb/articles`, `GET /kb/categories`, `GET /kb/articles/search`

### Examples

- `GET /users` in Docker → identity-service returns `500 INTERNAL_ERROR` (GlobalExceptionHandler catches as generic `Exception`)
- `GET /tickets` in Docker → ticket-service returns `400 BAD_REQUEST` with message: "Name for argument of type [int] not specified, and parameter name information not available via reflection."
- `GET /problems?page=0&size=10` in Docker → problem-service returns `400 BAD_REQUEST` with same reflection error (even explicit values fail because Spring cannot bind the parameter name)
- `GET /kb/articles/search?q=test` in Docker → knowledge-service returns `400 BAD_REQUEST` for the `page`/`size` parameters
- `POST /tickets` in Docker → works correctly (no `@RequestParam` with defaultValue, not affected)

## Expected Behavior

### Preservation Requirements

**Unchanged Behaviors:**
- All non-paginated endpoints (`POST`, `GET /{id}`, `PUT /{id}`, `DELETE /{id}`) must continue to function identically
- Local development (without `docker` Spring profile) must continue to work with `application.yml` defaults
- All existing unit and integration tests must pass without modification
- Kafka consumer retry and backoff settings from spec 2026.000006 in ticket-service and notification-service must be preserved
- DLQ (Dead Letter Queue) routing in notification-service must continue to route failed messages correctly
- Paginated endpoints called with explicit query parameter values (e.g., `?page=1&size=10`) must continue to respect those values

**Scope:**
All inputs that do NOT target paginated list endpoints should be completely unaffected by this fix. This includes:
- CRUD operations on individual resources (`POST`, `GET /{id}`, `PUT`, `DELETE`)
- Authentication endpoints (`POST /auth/login`, `POST /auth/refresh`)
- Non-paginated query endpoints
- Kafka event publishing and consumption
- SLA escalation cron jobs

## Hypothesized Root Cause

Based on the bug description and codebase analysis, the root cause is confirmed:

1. **Missing `-parameters` compiler flag (PRIMARY)**: The `maven-compiler-plugin` in the root `pom.xml` (line ~165) is configured with `<source>`, `<target>`, and `<encoding>` but does NOT include `<parameters>true</parameters>`. This means `javac` compiles all modules without retaining method parameter names in bytecode. When Spring Boot encounters `@RequestParam(defaultValue = "0") int page`, it cannot determine that the parameter is named `page` — it only sees `arg0`, `arg1`, etc. This triggers a `MethodArgumentResolutionException`.

2. **Inconsistent error responses across services**: identity-service has a `GlobalExceptionHandler` with a catch-all `@ExceptionHandler(Exception.class)` that maps any unhandled exception to `500 INTERNAL_ERROR`. Other services let Spring's default error handling return `400 BAD_REQUEST` with the descriptive reflection error message. This is not a bug to fix — it is an observation explaining the different error codes.

3. **Missing `application-docker.yml` for 7 services (SECONDARY)**: Only ticket-service and notification-service have `application-docker.yml` (created in spec 2026.000006). The remaining 7 services (identity-service, api-gateway, problem-service, change-service, asset-service, sla-service, knowledge-service) rely entirely on `docker-compose.yml` environment variable overrides. While this works today, it is a latent risk — if `docker-compose.yml` stops injecting a variable, the service falls back to `application.yml` defaults (e.g., `localhost:9092` for Kafka instead of `kafka:29092`).

## Correctness Properties

Property 1: Bug Condition — Paginated Endpoints Resolve Parameter Names

_For any_ HTTP request targeting a paginated list endpoint where `@RequestParam` parameters use `defaultValue` without explicit `value` attribute, the fixed application (compiled with `-parameters` flag) SHALL resolve parameter names via reflection and return HTTP 200 with a valid `PagedResponse` containing the correct paginated data.

**Validates: Requirements 2.1, 2.2, 2.3**

Property 2: Preservation — Non-Paginated Endpoint Behavior

_For any_ HTTP request that does NOT target a paginated list endpoint (CRUD operations, authentication, individual resource lookups), the fixed application SHALL produce exactly the same response as the original application, preserving all existing functionality for non-paginated interactions.

**Validates: Requirements 3.1, 3.2, 3.3, 3.6**

## Fix Implementation

### Changes Required

Assuming our root cause analysis is correct:

**File**: `pom.xml` (root)

**Plugin**: `maven-compiler-plugin` (inside `<pluginManagement>`)

**Specific Changes**:
1. **Add `-parameters` flag**: Add `<parameters>true</parameters>` inside the existing `<configuration>` block of the `maven-compiler-plugin`. This single line propagates to all 9 child modules via Maven's plugin management inheritance.

   Before:
   ```xml
   <configuration>
       <source>${java.version}</source>
       <target>${java.version}</target>
       <encoding>UTF-8</encoding>
   </configuration>
   ```

   After:
   ```xml
   <configuration>
       <source>${java.version}</source>
       <target>${java.version}</target>
       <encoding>UTF-8</encoding>
       <parameters>true</parameters>
   </configuration>
   ```

**Files**: 7 new `application-docker.yml` files

**Specific Changes**:
2. **Create `application-docker.yml` for 7 missing services**: Each file follows the same minimal pattern established in ticket-service and notification-service:

   ```yaml
   spring:
     kafka:
       bootstrap-servers: ${KAFKA_BOOTSTRAP_SERVERS:kafka:29092}
   ```

   Services requiring the file:
   - `identity-service/src/main/resources/application-docker.yml`
   - `api-gateway/src/main/resources/application-docker.yml`
   - `problem-service/src/main/resources/application-docker.yml`
   - `change-service/src/main/resources/application-docker.yml`
   - `asset-service/src/main/resources/application-docker.yml`
   - `sla-service/src/main/resources/application-docker.yml`
   - `knowledge-service/src/main/resources/application-docker.yml`

## Testing Strategy

### Validation Approach

The testing strategy follows a two-phase approach: first, surface counterexamples that demonstrate the bug on unfixed code, then verify the fix works correctly and preserves existing behavior.

### Exploratory Bug Condition Checking

**Goal**: Surface counterexamples that demonstrate the bug BEFORE implementing the fix. Confirm that the missing `-parameters` flag is the root cause.

**Test Plan**: Write unit tests that instantiate controller methods and verify Spring's parameter resolution behavior. Run these tests on the UNFIXED code to observe failures.

**Test Cases**:
1. **Parameter Name Resolution Test**: Use reflection to check whether compiled controller method parameters retain their names (will fail on unfixed code — names will be `arg0`, `arg1`, etc.)
2. **identity-service Paginated Endpoint Test**: Call `GET /users` with a mock MVC setup and verify the response (will fail with 500 on unfixed code)
3. **ticket-service Paginated Endpoint Test**: Call `GET /tickets` with a mock MVC setup and verify the response (will fail with 400 on unfixed code)
4. **Explicit Parameters Test**: Call `GET /users?page=0&size=10` to verify even explicit values fail when parameter names cannot be resolved (will fail on unfixed code)

**Expected Counterexamples**:
- Reflection on controller method parameters returns `arg0`, `arg1` instead of `page`, `size`, `sort`, `order`
- Spring throws `MethodArgumentResolutionException` when binding `@RequestParam` parameters
- Possible cause confirmed: missing `<parameters>true</parameters>` in `maven-compiler-plugin`

### Fix Checking

**Goal**: Verify that for all inputs where the bug condition holds, the fixed application produces the expected behavior.

**Pseudocode:**
```
FOR ALL input WHERE isBugCondition(input) DO
  result := handleRequest_fixed(input)
  ASSERT result.status = 200
  ASSERT result.body IS valid PagedResponse
  ASSERT result.body.content IS list
  ASSERT result.body.totalElements >= 0
  ASSERT result.body.page = input.queryParam("page", 0)
  ASSERT result.body.size = input.queryParam("size", 50)
END FOR
```

### Preservation Checking

**Goal**: Verify that for all inputs where the bug condition does NOT hold, the fixed application produces the same result as the original application.

**Pseudocode:**
```
FOR ALL input WHERE NOT isBugCondition(input) DO
  ASSERT handleRequest_original(input) = handleRequest_fixed(input)
END FOR
```

**Testing Approach**: Property-based testing is recommended for preservation checking because:
- It generates many test cases automatically across the input domain (various HTTP methods, paths, headers)
- It catches edge cases that manual unit tests might miss (unusual query parameter combinations)
- It provides strong guarantees that behavior is unchanged for all non-paginated inputs

**Test Plan**: Observe behavior on UNFIXED code first for non-paginated endpoints (POST, GET by ID, PUT, DELETE), then write property-based tests capturing that behavior.

**Test Cases**:
1. **Non-Paginated Endpoint Preservation**: Verify that `POST /tickets`, `GET /tickets/{id}`, `PUT /tickets/{id}`, `DELETE /tickets/{id}` continue to work identically after the fix
2. **Authentication Endpoint Preservation**: Verify that `POST /auth/login` and `POST /auth/refresh` continue to work identically
3. **Existing Test Suite Preservation**: Run `mvn test` across all modules and verify all tests pass
4. **Kafka Config Preservation**: Verify that ticket-service and notification-service `application-docker.yml` files are unchanged and Kafka reconnection settings from spec 2026.000006 are intact

### Unit Tests

- Test that compiled controller method parameters retain names when `-parameters` flag is enabled (reflection-based)
- Test each paginated endpoint with MockMvc to verify HTTP 200 and valid `PagedResponse` structure
- Test edge cases: `page` < 0, `size` > 500, `size` = 0, missing query parameters (defaults applied)
- Test that `application-docker.yml` files exist for all 9 services with correct Kafka bootstrap-servers

### Property-Based Tests

- Generate random valid pagination parameters (`page` in [0, 100], `size` in [1, 500], `sort` in valid fields, `order` in [ASC, DESC]) and verify all paginated endpoints return valid `PagedResponse` structures
- Generate random non-paginated HTTP requests and verify behavior is identical before and after the fix
- Generate random combinations of explicit and default query parameters and verify correct parameter binding

### Integration Tests

- Test full Docker Compose deployment with `docker compose up` and verify all 17 paginated endpoints return HTTP 200
- Test that each of the 9 services starts successfully with `SPRING_PROFILES_ACTIVE=docker` and loads `application-docker.yml`
- Test that Kafka event publishing and consumption works correctly after the fix (preservation of spec 2026.000006 changes)
