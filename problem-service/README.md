# Problem Service

ITIL Problem management microservice for the GLPI backend.

## Purpose

Manages the full lifecycle of ITIL Problems, including:
- Problem creation, update, and deletion
- Linking problems to tickets (many-to-many) and assets
- Root cause analysis fields: impactContent, causeContent, symptomContent
- Followups, tasks, and solutions
- Status lifecycle with transition enforcement
- Domain event publication to Kafka

## Port

`8083` (host and container)

## Local Setup

```bash
# Start infrastructure
docker compose up mongodb kafka -d

# Run the service
cd problem-service
mvn spring-boot:run
```

## Environment Variables

| Variable | Default | Description |
|---|---|---|
| `MONGODB_URI` | `mongodb://localhost:27017/problem_db` | MongoDB connection URI |
| `KAFKA_BOOTSTRAP_SERVERS` | `localhost:9092` | Kafka broker address |

## REST Endpoints

### Problems
| Method | Path | Description |
|---|---|---|
| `GET` | `/problems` | List problems (paginated) |
| `POST` | `/problems` | Create a problem |
| `GET` | `/problems/{id}` | Get problem by ID |
| `PUT` | `/problems/{id}` | Update problem |
| `PATCH` | `/problems/{id}` | Partially update problem |
| `DELETE` | `/problems/{id}` | Delete problem |

### Actors
| Method | Path | Description |
|---|---|---|
| `GET` | `/problems/{id}/actors` | List actors |
| `POST` | `/problems/{id}/actors` | Add actor |
| `DELETE` | `/problems/{id}/actors/{actorId}` | Remove actor |

### Followups
| Method | Path | Description |
|---|---|---|
| `GET` | `/problems/{id}/followups` | List followups |
| `POST` | `/problems/{id}/followups` | Add followup |

### Tasks
| Method | Path | Description |
|---|---|---|
| `GET` | `/problems/{id}/tasks` | List tasks |
| `POST` | `/problems/{id}/tasks` | Add task |

### Solutions
| Method | Path | Description |
|---|---|---|
| `GET` | `/problems/{id}/solutions` | Get solution |
| `POST` | `/problems/{id}/solutions` | Add solution (transitions to SOLVED) |

### Linked Tickets
| Method | Path | Description |
|---|---|---|
| `GET` | `/problems/{id}/tickets` | List linked ticket IDs |
| `POST` | `/problems/{id}/tickets` | Link a ticket to the problem |
| `DELETE` | `/problems/{id}/tickets/{ticketId}` | Unlink a ticket |

## Problem Status Values

| Status | Value |
|---|---|
| INCOMING | 1 |
| ASSIGNED | 2 |
| PLANNED | 3 |
| WAITING | 4 |
| SOLVED | 5 |
| CLOSED | 6 |
| ACCEPTED | 7 |
| OBSERVED | 8 |

## Kafka Topics

| Topic | Events Published |
|---|---|
| `problems.events` | `ProblemCreated`, `ProblemUpdated`, `ProblemTicketLinked`, `ProblemSolved`, `ProblemClosed` |

## API Documentation

- OpenAPI spec: `GET /v3/api-docs`
- Swagger UI: `GET /swagger-ui.html`
- Health: `GET /actuator/health`
