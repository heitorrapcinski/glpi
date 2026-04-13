# Bugfix Requirements Document

## Introduction

When the GLPI Microservices Backend runs in Docker Compose, the API Gateway's Swagger UI at `http://localhost:8080/swagger-ui.html` fails to load OpenAPI specs from downstream services. The `springdoc.swaggerui.urls` configuration in `api-gateway/src/main/resources/application.yml` uses environment variables (e.g., `ASSET_SERVICE_URL`) that resolve to internal Docker service hostnames (e.g., `http://asset-service:8085`). Since Swagger UI runs in the browser, it attempts to fetch specs directly from these internal URLs, which the browser cannot resolve. This causes DNS resolution failures and CORS errors, rendering Swagger UI unusable in Docker Compose environments.

## Bug Analysis

### Current Behavior (Defect)

1.1 WHEN the application runs in Docker Compose AND a user accesses `http://localhost:8080/swagger-ui.html` THEN the system returns Swagger UI with `springdoc.swaggerui.urls` pointing to internal Docker hostnames (e.g., `http://asset-service:8085/v3/api-docs`)

1.2 WHEN Swagger UI attempts to fetch an OpenAPI spec from an internal Docker hostname (e.g., `http://asset-service:8085/v3/api-docs`) THEN the browser fails with a DNS resolution error because it cannot resolve the internal service hostname

1.3 WHEN Swagger UI attempts to fetch an OpenAPI spec from an internal Docker hostname that differs from the page origin (`http://localhost:8080`) THEN the browser reports a CORS error: "The URL origin does not match the page"

1.4 WHEN the application runs in Docker Compose THEN the system exposes no gateway routes for proxying downstream `/v3/api-docs` endpoints, so there is no gateway-routed alternative for Swagger UI to use

### Expected Behavior (Correct)

2.1 WHEN the application runs in Docker Compose AND a user accesses `http://localhost:8080/swagger-ui.html` THEN the system SHALL return Swagger UI with `springdoc.swaggerui.urls` pointing to gateway-relative paths (e.g., `/identity-service/v3/api-docs`) that the browser can resolve through the gateway

2.2 WHEN Swagger UI attempts to fetch an OpenAPI spec through a gateway-relative path (e.g., `/identity-service/v3/api-docs`) THEN the system SHALL proxy the request to the corresponding downstream service and return the OpenAPI spec successfully

2.3 WHEN Swagger UI fetches OpenAPI specs through gateway-relative paths THEN the system SHALL NOT produce any CORS errors because all requests stay within the same origin (`http://localhost:8080`)

2.4 WHEN the application runs in Docker Compose THEN the system SHALL expose gateway routes that proxy `/v3/api-docs` requests for each downstream service (identity-service, ticket-service, problem-service, change-service, asset-service, sla-service, notification-service, knowledge-service)

### Unchanged Behavior (Regression Prevention)

3.1 WHEN the application runs locally without Docker Compose (using default localhost URLs) THEN the system SHALL CONTINUE TO serve Swagger UI and load OpenAPI specs from all downstream services correctly

3.2 WHEN a client sends API requests to existing gateway routes (e.g., `/tickets/**`, `/assets/**`, `/auth/**`) THEN the system SHALL CONTINUE TO proxy those requests to the correct downstream services without any change in behavior

3.3 WHEN the application runs in Docker Compose THEN the gateway SHALL CONTINUE TO route all existing API traffic (non-Swagger) to downstream services using internal Docker hostnames as before

3.4 WHEN a user accesses the gateway's own OpenAPI spec at `/v3/api-docs` THEN the system SHALL CONTINUE TO return the gateway's own API documentation without interference from the new routes

---

## Bug Condition

### Bug Condition Function

```pascal
FUNCTION isBugCondition(X)
  INPUT: X of type SwaggerUIRequest
  OUTPUT: boolean

  // The bug triggers when Swagger UI runs in a browser and the configured
  // springdoc.swaggerui.urls contain internal Docker hostnames that the
  // browser cannot resolve (i.e., the URL host is not localhost/127.0.0.1)
  RETURN X.swaggerUrlHost ≠ "localhost" AND X.swaggerUrlHost ≠ "127.0.0.1"
END FUNCTION
```

### Property Specification — Fix Checking

```pascal
// Property: Fix Checking — Swagger UI fetches specs through gateway-relative paths
FOR ALL X WHERE isBugCondition(X) DO
  result ← fetchSwaggerSpec'(X)
  ASSERT result.url IS relative_path (starts with "/")
    AND result.status = 200
    AND no_cors_error(result)
    AND result.body IS valid_openapi_spec
END FOR
```

### Property Specification — Preservation Checking

```pascal
// Property: Preservation Checking — Non-Swagger API routing is unchanged
FOR ALL X WHERE NOT isBugCondition(X) DO
  ASSERT routeRequest(X) = routeRequest'(X)
END FOR
```
