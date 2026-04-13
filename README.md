# GLPI Microservices Backend

A cloud-native ITSM backend that reimplements the GLPI v12 domain as a set of independent microservices. Built with **Java 21**, **Spring Boot 3.x**, **MongoDB 7**, and **Apache Kafka 3**, following Hexagonal Architecture and Domain-Driven Design principles.

---

## Architecture Overview

```
                        ┌─────────────────────────────────────────────────────┐
                        │              GLPI Microservices Backend              │
                        │                                                     │
  API Client ──HTTPS──► │  API Gateway (8080)                                 │
                        │       │                                             │
                        │       ├──► Identity Service  (8081)                 │
                        │       ├──► Ticket Service    (8082)                 │
                        │       ├──► Problem Service   (8083)                 │
                        │       ├──► Change Service    (8084)                 │
                        │       ├──► Asset Service     (8085)                 │
                        │       ├──► SLA Service       (8086)                 │
                        │       ├──► Notification Svc  (8087)                 │
                        │       └──► Knowledge Service (8088)                 │
                        │                                                     │
                        │  Apache Kafka ◄──── domain events ────► services    │
                        │  MongoDB (one database per service)                 │
                        └─────────────────────────────────────────────────────┘
```

Each service follows the same internal hexagonal structure:

```
src/main/java/com/glpi/{service}/
├── domain/
│   ├── model/          # Aggregates, entities, value objects
│   ├── port/in/        # Use case interfaces (driving ports)
│   └── port/out/       # Repository & event interfaces (driven ports)
├── application/usecase/ # Application-layer orchestration
├── adapter/
│   ├── in/rest/        # REST controllers
│   └── out/
│       ├── persistence/ # MongoDB repositories
│       └── messaging/   # Kafka producers
└── config/             # Spring configuration
```

---

## Microservices

| Service | Port | Description | README |
|---|---|---|---|
| API Gateway | 8080 | Single entry point: routing, JWT validation, rate limiting | [api-gateway/README.md](api-gateway/README.md) |
| Identity Service | 8081 | Users, entities, profiles, groups, authentication, JWT | [identity-service/README.md](identity-service/README.md) |
| Ticket Service | 8082 | Incidents and service requests (ITIL tickets) | [ticket-service/README.md](ticket-service/README.md) |
| Problem Service | 8083 | ITIL problems and root cause analysis | [problem-service/README.md](problem-service/README.md) |
| Change Service | 8084 | ITIL changes with approval workflows | [change-service/README.md](change-service/README.md) |
| Asset Service | 8085 | CMDB assets, software, and license management | [asset-service/README.md](asset-service/README.md) |
| SLA Service | 8086 | SLA/OLA definitions, calendars, and escalation | [sla-service/README.md](sla-service/README.md) |
| Notification Service | 8087 | Event-driven notifications via email and webhook | [notification-service/README.md](notification-service/README.md) |
| Knowledge Service | 8088 | Knowledge base articles, categories, and FAQ | [knowledge-service/README.md](knowledge-service/README.md) |

---

## Infrastructure

| Component | Port | Purpose |
|---|---|---|
| MongoDB 7.x | 27017 | Persistent storage (one database per service) |
| Apache Kafka 3.x | 9092 | Domain event bus |
| Zookeeper | 2181 | Kafka coordination |

---

## Getting Started

### Prerequisites

- Docker 24+ and Docker Compose v2
- Java 21 (for local development without Docker)
- Maven 3.9+ (for local development without Docker)

### Start the full stack

```bash
# Start all infrastructure and microservices
docker compose up -d

# Check service health
docker compose ps

# View logs for a specific service
docker compose logs -f identity-service
```

### Run seeders (populate default data)

```bash
# Seeds default profiles, users, root entity, and SLA calendar
docker compose --profile seed up
```

### Start infrastructure only (for local development)

```bash
docker compose up -d zookeeper kafka mongodb
```

Then run individual services from your IDE or with:

```bash
mvn -pl identity-service spring-boot:run
```

---

## Environment Variables

Copy `.env.example` to `.env` and adjust values before starting:

```bash
cp .env.example .env
```

Key variables:

| Variable | Default | Description |
|---|---|---|
| `MONGODB_URI` | `mongodb://glpi:glpi_secret@mongodb:27017` | MongoDB connection URI |
| `KAFKA_BOOTSTRAP_SERVERS` | `kafka:29092` | Kafka broker address |
| `JWT_PRIVATE_KEY` | _(empty)_ | RS256 private key (PEM, base64-encoded) |
| `JWT_PUBLIC_KEY` | _(empty)_ | RS256 public key (PEM, base64-encoded) |
| `BCRYPT_COST` | `12` | BCrypt cost factor for password hashing |
| `SMTP_HOST` | `localhost` | SMTP server host for email notifications |

---

## Kafka Topics

| Topic | Producers | Consumers |
|---|---|---|
| `identity.users` | Identity | Notification, Ticket, Asset |
| `identity.entities` | Identity | All services |
| `identity.profiles` | Identity | API Gateway |
| `tickets.events` | Ticket | Notification, SLA, Problem, Change |
| `problems.events` | Problem | Notification, Change |
| `changes.events` | Change | Notification |
| `assets.events` | Asset | Notification, Ticket |
| `sla.events` | SLA | Notification, Ticket |
| `knowledge.events` | Knowledge | Notification |
| `*.dlq` | All (DLQ router) | Ops/alerting |

---

## Building

```bash
# Build all modules
mvn clean package -DskipTests

# Build and run tests
mvn clean verify

# Build a specific service
mvn -pl identity-service clean package
```

---

## Technology Stack

| Concern | Technology |
|---|---|
| Language | Java 21 (virtual threads via Project Loom) |
| Framework | Spring Boot 3.x |
| Database | MongoDB 7.x (one database per service) |
| Messaging | Apache Kafka 3.x |
| API Gateway | Spring Cloud Gateway |
| Auth | JWT RS256 (Spring Security + JJWT) |
| API Docs | SpringDoc OpenAPI 3.0 |
| Build | Maven (multi-module) |
| Containerization | Docker + Docker Compose |
| Testing (unit) | JUnit 5 + Mockito |
| Testing (property) | jqwik |

---

## License

This project is a reimplementation of GLPI domain logic. See the original GLPI project for licensing information.
