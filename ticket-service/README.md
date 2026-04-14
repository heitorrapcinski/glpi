# Ticket Service

ITIL Incident and Service Request management microservice for the GLPI backend.

## Purpose

Manages the full lifecycle of ITIL tickets (Incidents and Service Requests), including:
- Ticket creation, update, assignment, and soft deletion
- Followups, tasks, and solutions
- Validation workflow (request, approve, refuse)
- SLA/OLA deadline tracking with timer pause/resume on WAITING status
- Priority matrix computation (urgency × impact)
- Domain event publication to Kafka

## Port

`8082` (host and container)

## Local Setup

```bash
# Start infrastructure
docker compose up mongodb kafka -d

# Run the service
cd ticket-service
mvn spring-boot:run
```

## Environment Variables

| Variable | Default | Description |
|---|---|---|
| `MONGODB_URI` | `mongodb://localhost:27017/ticket_db` | MongoDB connection URI |
| `KAFKA_BOOTSTRAP_SERVERS` | `localhost:9092` | Kafka broker address |
| `SLA_SERVICE_URL` | `http://localhost:8086` | SLA Service base URL for deadline computation |

## REST Endpoints

### Tickets
| Method | Path | Description |
|---|---|---|
| `GET` | `/tickets` | List tickets (paginated) |
| `POST` | `/tickets` | Create a ticket |
| `GET` | `/tickets/{id}` | Get ticket by ID |
| `PUT` | `/tickets/{id}` | Update ticket |
| `PATCH` | `/tickets/{id}` | Partially update ticket |
| `DELETE` | `/tickets/{id}` | Soft-delete ticket |

### Actors
| Method | Path | Description |
|---|---|---|
| `GET` | `/tickets/{id}/actors` | List actors |
| `POST` | `/tickets/{id}/actors` | Add actor |
| `DELETE` | `/tickets/{id}/actors/{actorId}` | Remove actor |

### Followups
| Method | Path | Description |
|---|---|---|
| `GET` | `/tickets/{id}/followups` | List followups |
| `POST` | `/tickets/{id}/followups` | Add followup |

### Tasks
| Method | Path | Description |
|---|---|---|
| `GET` | `/tickets/{id}/tasks` | List tasks |
| `POST` | `/tickets/{id}/tasks` | Add task |
| `PUT` | `/tickets/{id}/tasks/{taskId}` | Update task |

### Solutions
| Method | Path | Description |
|---|---|---|
| `GET` | `/tickets/{id}/solutions` | Get solution |
| `POST` | `/tickets/{id}/solutions` | Add solution (transitions to SOLVED) |
| `POST` | `/tickets/{id}/solutions/reject` | Reject solution (reopens to ASSIGNED) |

### Validations
| Method | Path | Description |
|---|---|---|
| `GET` | `/tickets/{id}/validations` | List validations |
| `POST` | `/tickets/{id}/validations` | Request validation |
| `PUT` | `/tickets/{id}/validations/{validationId}` | Approve or refuse validation |

## Kafka Topics

| Topic | Events Published |
|---|---|
| `tickets.events` | `TicketCreated`, `TicketUpdated`, `TicketDeleted`, `TicketSolved`, `TicketReopened`, `TicketFollowupAdded`, `TicketTaskCompleted`, `TicketValidationRequested`, `TicketValidationApproved`, `TicketValidationRefused` |

## Default Seeded Data

On first startup (empty database), `TicketSeeder` seeds:

- **1 default ITIL priority matrix** (id=`default`, entity=`0`): 5×5 urgency × impact grid mapping to priorities 1–6

## API Documentation

- OpenAPI spec: `GET /v3/api-docs`
- Swagger UI: `GET /swagger-ui.html`
- Health: `GET /actuator/health`
