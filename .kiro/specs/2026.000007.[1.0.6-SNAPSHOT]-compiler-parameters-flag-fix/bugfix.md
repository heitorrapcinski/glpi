# Bugfix Requirements Document

## Introduction

All paginated list endpoints across every microservice fail at runtime when deployed in Docker via `docker compose up`. The root cause is the `maven-compiler-plugin` configuration in the root `pom.xml`, which is missing the `-parameters` compiler flag. Without this flag, Java does not retain method parameter names in the compiled bytecode, causing Spring Boot's `@RequestParam` resolution to fail when parameter names are not explicitly specified via the `value` attribute.

The failure manifests differently depending on each service's error handling:
- **identity-service** returns `500 INTERNAL_ERROR` because its `GlobalExceptionHandler` catches the underlying exception as a generic `Exception`.
- **ticket-service**, **problem-service**, and all other affected services return `400 BAD_REQUEST` with the message: *"Name for argument of type [int] not specified, and parameter name information not available via reflection. Ensure that the compiler uses the '-parameters' flag."*

A secondary issue exists: 7 services (identity-service, api-gateway, problem-service, change-service, asset-service, sla-service, knowledge-service) are missing `application-docker.yml` Docker profile files. While the current `docker-compose.yml` injects environment variables that override the defaults, the absence of these files is a latent configuration risk — the same pattern already fixed for ticket-service and notification-service in spec 2026.000006.

## Bug Analysis

### Current Behavior (Defect)

1.1 WHEN a paginated list endpoint is called in Docker (e.g., `GET /users`, `GET /tickets`, `GET /problems`) AND the `@RequestParam` parameters (`page`, `size`, `sort`, `order`) do not specify an explicit `value` attribute THEN the system fails because the compiled bytecode does not contain method parameter names, and Spring cannot resolve the parameter binding.

1.2 WHEN identity-service's paginated endpoints are called in Docker (e.g., `GET /users`, `GET /entities`, `GET /groups`, `GET /profiles`) THEN the system returns `500 INTERNAL_ERROR` because the `GlobalExceptionHandler` catches the `@RequestParam` resolution failure as a generic `Exception` and maps it to a 500 status.

1.3 WHEN ticket-service, problem-service, change-service, asset-service, sla-service, knowledge-service, or notification-service paginated endpoints are called in Docker THEN the system returns `400 BAD_REQUEST` with message "Name for argument of type [int] not specified, and parameter name information not available via reflection."

1.4 WHEN identity-service, api-gateway, problem-service, change-service, asset-service, sla-service, or knowledge-service are started with `SPRING_PROFILES_ACTIVE=docker` THEN the system has no `application-docker.yml` profile file, relying entirely on environment variable overrides for Docker-specific configuration.

### Expected Behavior (Correct)

2.1 WHEN a paginated list endpoint is called in Docker with default or explicit query parameters THEN the system SHALL correctly resolve `@RequestParam` parameter names via reflection and return a successful paginated response (HTTP 200).

2.2 WHEN identity-service's paginated endpoints are called in Docker THEN the system SHALL return `200 OK` with a valid `PagedResponse` containing the paginated data, not a 500 error.

2.3 WHEN ticket-service, problem-service, change-service, asset-service, sla-service, knowledge-service, or notification-service paginated endpoints are called in Docker THEN the system SHALL return `200 OK` with valid paginated data, not a 400 error.

2.4 WHEN identity-service, api-gateway, problem-service, change-service, asset-service, sla-service, or knowledge-service are started with `SPRING_PROFILES_ACTIVE=docker` THEN the system SHALL load a dedicated `application-docker.yml` profile file with Docker-specific defaults, consistent with the pattern established in ticket-service and notification-service.

### Unchanged Behavior (Regression Prevention)

3.1 WHEN any non-paginated endpoint is called (e.g., `POST /tickets`, `GET /tickets/{id}`, `POST /auth/login`) THEN the system SHALL CONTINUE TO function correctly with no change in behavior.

3.2 WHEN services are run locally outside Docker (without the `docker` Spring profile) THEN the system SHALL CONTINUE TO use the default `application.yml` configuration and operate normally.

3.3 WHEN all existing unit and integration tests are executed THEN the system SHALL CONTINUE TO pass without regressions.

3.4 WHEN the Kafka reconnection configuration added in spec 2026.000006 is active THEN the system SHALL CONTINUE TO preserve the Kafka consumer retry and backoff settings in ticket-service and notification-service.

3.5 WHEN the DLQ (Dead Letter Queue) routing in notification-service is active THEN the system SHALL CONTINUE TO route failed messages to the dead letter topic as configured.

3.6 WHEN paginated endpoints are called with explicit query parameter values (e.g., `?page=1&size=10`) THEN the system SHALL CONTINUE TO respect those values and return the correct page of results.

---

### Bug Condition (Formal)

```pascal
FUNCTION isBugCondition(X)
  INPUT: X of type HttpRequest targeting a paginated list endpoint
  OUTPUT: boolean

  // Returns true when the request targets a paginated endpoint
  // whose controller method uses @RequestParam without explicit value
  // and the bytecode was compiled without the -parameters flag
  RETURN X.endpoint IS paginated_list_endpoint
     AND X.runtime = "Docker"
     AND compiler_parameters_flag = false
END FUNCTION
```

### Fix Checking Property

```pascal
// Property: Fix Checking — Paginated endpoints resolve parameter names
FOR ALL X WHERE isBugCondition(X) DO
  result ← handleRequest'(X)
  ASSERT result.status = 200
     AND result.body IS valid PagedResponse
     AND no_parameter_resolution_error(result)
END FOR
```

### Preservation Checking Property

```pascal
// Property: Preservation Checking — Non-buggy inputs behave identically
FOR ALL X WHERE NOT isBugCondition(X) DO
  ASSERT handleRequest(X) = handleRequest'(X)
END FOR
```
