# GLPI Identity Service

Microservice responsible for Identity & Access Management (IAM) in the GLPI Microservices Backend.

## Purpose

Handles user lifecycle, entity hierarchy, profile-based RBAC, authentication (JWT RS256), Two-Factor Authentication (TOTP), and user impersonation.

## Local Setup

### Prerequisites

- Java 21
- Maven 3.9+
- MongoDB 7.x running on `localhost:27017`
- Kafka 3.x running on `localhost:9092` (optional for local dev)

### Run locally

```bash
# From the project root
mvn -pl common,identity-service -am spring-boot:run
```

The service starts on port **8081**.

### Run with Docker Compose

```bash
docker compose up identity-service
```

## Environment Variables

| Variable | Default | Description |
|---|---|---|
| `MONGODB_URI` | `mongodb://localhost:27017/identity_db` | MongoDB connection URI |
| `KAFKA_BOOTSTRAP_SERVERS` | `localhost:9092` | Kafka broker address |
| `JWT_PRIVATE_KEY` | *(ephemeral dev key)* | Base64-encoded PKCS8 RSA private key |
| `JWT_PUBLIC_KEY` | *(ephemeral dev key)* | Base64-encoded X509 RSA public key |
| `AES_ENCRYPTION_KEY` | *(random dev key)* | Base64-encoded 32-byte AES-256 key |
| `SERVER_PORT` | `8081` | HTTP port |

> **Warning**: The ephemeral dev keys are regenerated on every restart. Set `JWT_PRIVATE_KEY` and `JWT_PUBLIC_KEY` in production.

## API Endpoints

### Authentication

| Method | Path | Description |
|---|---|---|
| `POST` | `/auth/login` | Authenticate with username + password (+ optional TOTP) |
| `POST` | `/auth/refresh` | Rotate refresh token and get new access token |
| `POST` | `/auth/logout` | Blocklist current JWT |

### Users

| Method | Path | Description |
|---|---|---|
| `POST` | `/users` | Create user |
| `GET` | `/users` | List users (paginated) |
| `GET` | `/users/{id}` | Get user by ID |
| `PUT` | `/users/{id}` | Update user |
| `DELETE` | `/users/{id}` | Deactivate user |
| `DELETE` | `/users/{id}/purge` | Hard-delete user |
| `POST` | `/users/{id}/api-token` | Generate personal API token |
| `POST` | `/users/{id}/impersonate` | Impersonate user (requires IMPERSONATE right) |

### Entities

| Method | Path | Description |
|---|---|---|
| `GET` | `/entities` | List entities (paginated) |
| `POST` | `/entities` | Create entity |
| `GET` | `/entities/{id}` | Get entity by ID |
| `PUT` | `/entities/{id}` | Update entity |
| `DELETE` | `/entities/{id}` | Delete entity |

### Profiles

| Method | Path | Description |
|---|---|---|
| `GET` | `/profiles` | List profiles (paginated) |
| `POST` | `/profiles` | Create profile |
| `GET` | `/profiles/{id}` | Get profile by ID |
| `PUT` | `/profiles/{id}` | Update profile |
| `DELETE` | `/profiles/{id}` | Delete profile |
| `POST` | `/profiles/{id}/assign` | Assign profile to user in entity |

### Groups

| Method | Path | Description |
|---|---|---|
| `GET` | `/groups` | List groups (paginated) |
| `POST` | `/groups` | Create group |
| `GET` | `/groups/{id}` | Get group by ID |
| `PUT` | `/groups/{id}` | Update group |
| `DELETE` | `/groups/{id}` | Delete group |

## API Documentation

- OpenAPI spec: `GET /v3/api-docs`
- Swagger UI: `GET /swagger-ui.html`

## Kafka Topics

| Topic | Role | Events |
|---|---|---|
| `identity.users` | Producer | `UserPurged`, `AccountLocked` |
| `identity.profiles` | Producer | `ProfileAssigned` |

## Default Seeded Data

On first startup (empty database), `IdentitySeeder` seeds:

- **Root entity** (id=`0`, name=`Root Entity`)
- **8 default profiles**: Self-Service, Observer, Admin, Super-Admin, Hotliner, Technician, Supervisor, Read-Only
- **4 default users**: `glpi/glpi`, `post-only/postonly`, `tech/tech`, `normal/normal`
