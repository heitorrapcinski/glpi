# API Gateway

Edge service for the GLPI Microservices Backend. Handles JWT validation, App-Token validation, rate limiting, request routing, CORS, health aggregation, request logging, and OpenAPI aggregation.

## Purpose

The API Gateway is the single entry point for all API clients. It:
- Validates RS256 JWT tokens and forwards user context as headers to downstream services
- Validates `App-Token` headers for API client authentication
- Enforces rate limiting (1000 requests/minute per user or IP)
- Routes requests to the appropriate downstream microservice
- Aggregates health status from all downstream services
- Exposes a unified Swagger UI combining all service OpenAPI specs
- Logs all requests with timestamp, method, path, user ID, status, and latency

## Routing Table

| Path Prefix | Downstream Service | Default URL |
|---|---|---|
| `/auth/**`, `/users/**`, `/entities/**`, `/profiles/**`, `/groups/**` | Identity Service | `http://localhost:8081` |
| `/tickets/**` | Ticket Service | `http://localhost:8082` |
| `/problems/**` | Problem Service | `http://localhost:8083` |
| `/changes/**` | Change Service | `http://localhost:8084` |
| `/assets/**` | Asset Service | `http://localhost:8085` |
| `/slas/**`, `/olas/**`, `/calendars/**` | SLA Service | `http://localhost:8086` |
| `/notifications/**` | Notification Service | `http://localhost:8087` |
| `/knowledge/**` | Knowledge Service | `http://localhost:8088` |

## Public Endpoints (no JWT required)

- `POST /auth/login`
- `POST /auth/refresh`

## Rate Limiting

- **Limit**: 1000 requests per 60-second window
- **Key**: `X-User-Id` header (or remote IP if not present)
- **Response on limit exceeded**: HTTP 429 with `Retry-After: 60` header
- **Implementation**: In-memory token bucket (dev). For production, replace with Redis-backed implementation.

## Environment Variables

| Variable | Default | Description |
|---|---|---|
| `JWT_PUBLIC_KEY` | *(ephemeral dev key)* | Base64-encoded X509 RSA public key (PEM without headers) |
| `GATEWAY_APP_TOKENS` | `dev-app-token-1,dev-app-token-2` | Comma-separated list of valid API client tokens |
| `IDENTITY_SERVICE_URL` | `http://localhost:8081` | Identity Service base URL |
| `TICKET_SERVICE_URL` | `http://localhost:8082` | Ticket Service base URL |
| `PROBLEM_SERVICE_URL` | `http://localhost:8083` | Problem Service base URL |
| `CHANGE_SERVICE_URL` | `http://localhost:8084` | Change Service base URL |
| `ASSET_SERVICE_URL` | `http://localhost:8085` | Asset Service base URL |
| `SLA_SERVICE_URL` | `http://localhost:8086` | SLA Service base URL |
| `NOTIFICATION_SERVICE_URL` | `http://localhost:8087` | Notification Service base URL |
| `KNOWLEDGE_SERVICE_URL` | `http://localhost:8088` | Knowledge Service base URL |

## Local Setup

```bash
# Build and run with Maven
mvn -pl common,api-gateway -am spring-boot:run

# Or with Docker
docker build -f api-gateway/Dockerfile -t glpi-api-gateway .
docker run -p 8080:8080 glpi-api-gateway
```

## Forwarded Headers

After JWT validation, the gateway adds these headers to downstream requests:

| Header | Value |
|---|---|
| `X-User-Id` | JWT `sub` claim (user ID) |
| `X-Entity-Id` | JWT `entity_id` claim |
| `X-Profile-Id` | JWT `profile_id` claim |
| `X-User-Rights` | Base64-encoded JSON rights map from JWT `rights` claim |

## API Documentation

- Swagger UI: `http://localhost:8080/swagger-ui.html`
- OpenAPI spec: `http://localhost:8080/v3/api-docs`
- Health: `http://localhost:8080/actuator/health`
