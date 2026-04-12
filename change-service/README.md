# Change Service

ITIL Change management microservice for the GLPI backend.

## Purpose

Manages the full lifecycle of ITIL Changes, including:
- Change creation, update, and deletion
- Extended status workflow (INCOMING → EVALUATION → APPROVAL → ACCEPTED → TEST → QUALIFICATION → SOLVED → CLOSED)
- Planning documents: impactContent, controlListContent, rolloutPlanContent, backoutPlanContent, checklistContent
- Validation steps with approval workflow
- Linking changes to tickets, problems, and assets
- Followups, tasks, and solutions
- Satisfaction surveys on closed changes
- Status transition enforcement (INCOMING cannot go directly to SOLVED)
- Domain event publication to Kafka

## Port

`8084` (host and container)

## Local Setup

```bash
# Start infrastructure
docker compose up mongodb kafka -d

# Run the service
cd change-service
mvn spring-boot:run
```

## Environment Variables

| Variable | Default | Description |
|---|---|---|
| `MONGODB_URI` | `mongodb://localhost:27017/change_db` | MongoDB connection URI |
| `KAFKA_BOOTSTRAP_SERVERS` | `localhost:9092` | Kafka broker address |

## REST Endpoints

### Changes
| Method | Path | Description |
|---|---|---|
| `GET` | `/changes` | List changes (paginated) |
| `POST` | `/changes` | Create a change |
| `GET` | `/changes/{id}` | Get change by ID |
| `PUT` | `/changes/{id}` | Update change |
| `PATCH` | `/changes/{id}` | Partially update change |
| `DELETE` | `/changes/{id}` | Delete change |

### Actors
| Method | Path | Description |
|---|---|---|
| `GET` | `/changes/{id}/actors` | List actors |
| `POST` | `/changes/{id}/actors` | Add actor |
| `DELETE` | `/changes/{id}/actors/{actorId}` | Remove actor |

### Followups
| Method | Path | Description |
|---|---|---|
| `GET` | `/changes/{id}/followups` | List followups |
| `POST` | `/changes/{id}/followups` | Add followup |

### Tasks
| Method | Path | Description |
|---|---|---|
| `GET` | `/changes/{id}/tasks` | List tasks |
| `POST` | `/changes/{id}/tasks` | Add task |

### Solutions
| Method | Path | Description |
|---|---|---|
| `GET` | `/changes/{id}/solutions` | Get solution |
| `POST` | `/changes/{id}/solutions` | Add solution (transitions to SOLVED) |

### Validations
| Method | Path | Description |
|---|---|---|
| `GET` | `/changes/{id}/validations` | List validation steps |
| `POST` | `/changes/{id}/validations` | Add validation step |
| `PUT` | `/changes/{id}/validations/{validationId}` | Approve validation step |

### Linked Tickets
| Method | Path | Description |
|---|---|---|
| `GET` | `/changes/{id}/tickets` | List linked ticket IDs |
| `POST` | `/changes/{id}/tickets` | Link a ticket to the change |

### Linked Problems
| Method | Path | Description |
|---|---|---|
| `GET` | `/changes/{id}/problems` | List linked problem IDs |
| `POST` | `/changes/{id}/problems` | Link a problem to the change |

## Change Status Values

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
| EVALUATION | 9 |
| APPROVAL | 10 |
| TEST | 11 |
| QUALIFICATION | 12 |
| REFUSED | 13 |
| CANCELED | 14 |

## Status Transition Rules

- `INCOMING` cannot transition directly to `SOLVED` — must pass through `EVALUATION` or `APPROVAL` first
- Full transition matrix is enforced; invalid transitions return HTTP 422 with `INVALID_STATUS_TRANSITION`

## Kafka Topics

| Topic | Events Published |
|---|---|
| `changes.events` | `ChangeCreated`, `ChangeUpdated`, `ChangeValidationApproved`, `ChangeTicketLinked`, `ChangeSolved`, `ChangeClosed` |

## API Documentation

- OpenAPI spec: `GET /v3/api-docs`
- Swagger UI: `GET /swagger-ui.html`
- Health: `GET /actuator/health`
