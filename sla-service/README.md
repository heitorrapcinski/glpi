# GLPI SLA Service

Manages SLA/OLA definitions, business-hours calendars, deadline computation, and escalation scheduling.

## Purpose

- Define SLAs (Service Level Agreements) and OLAs (Operational Level Agreements) with escalation levels
- Manage business-hours calendars with segments and holidays
- Compute SLA deadlines accounting for business hours and holidays
- Evaluate and trigger SLA escalations on a configurable schedule

## Local Setup

```bash
# Start infrastructure
docker compose up mongodb kafka -d

# Run the service
cd sla-service
mvn spring-boot:run
```

Service starts on port **8086**.

## Environment Variables

| Variable | Default | Description |
|---|---|---|
| `MONGODB_URI` | `mongodb://localhost:27017/sla_db` | MongoDB connection URI |
| `KAFKA_BOOTSTRAP_SERVERS` | `localhost:9092` | Kafka broker address |
| `TICKET_SERVICE_URL` | `http://localhost:8082` | Ticket Service base URL for escalation queries |
| `ESCALATION_CRON` | `0 */5 * * * *` | Cron expression for escalation scheduler |

## Endpoints

### SLAs
| Method | Path | Description |
|---|---|---|
| GET | `/slas` | List SLAs (paginated) |
| POST | `/slas` | Create SLA |
| GET | `/slas/{id}` | Get SLA by ID |
| PUT | `/slas/{id}` | Update SLA |
| DELETE | `/slas/{id}` | Delete SLA |
| POST | `/slas/compute-deadline` | Compute deadline for given start, duration, and calendar |

### OLAs
| Method | Path | Description |
|---|---|---|
| GET | `/olas` | List OLAs (paginated) |
| POST | `/olas` | Create OLA |
| GET | `/olas/{id}` | Get OLA by ID |
| PUT | `/olas/{id}` | Update OLA |
| DELETE | `/olas/{id}` | Delete OLA |

### Calendars
| Method | Path | Description |
|---|---|---|
| GET | `/calendars` | List calendars (paginated) |
| POST | `/calendars` | Create calendar |
| GET | `/calendars/{id}` | Get calendar by ID |
| PUT | `/calendars/{id}` | Update calendar |
| DELETE | `/calendars/{id}` | Delete calendar |
| POST | `/calendars/{id}/holidays` | Add holiday to calendar |
| DELETE | `/calendars/{id}/holidays/{holidayId}` | Remove holiday from calendar |

## Kafka Topics

| Topic | Role | Event Types |
|---|---|---|
| `sla.events` | Producer | `SlaEscalationTriggered` |

## Default Seeded Data

On first startup (empty database), `SlaSeeder` seeds:

- **1 default calendar** (id=`1`, name=`Default`): Monday through Friday, 08:00–20:00

## API Documentation

- OpenAPI spec: `http://localhost:8086/v3/api-docs`
- Swagger UI: `http://localhost:8086/swagger-ui.html`
