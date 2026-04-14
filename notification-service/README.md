# Notification Service

Event-driven notification microservice for the GLPI backend.

## Purpose

Consumes domain events from Kafka and dispatches notifications to configured targets:
- Email delivery via SMTP (JavaMail)
- Webhook delivery via HTTP POST
- Template-based subject and body rendering
- Notification queue with status tracking (PENDING/SENT/FAILED)
- Retry with exponential backoff (1s, 4s, 16s) on delivery failure
- Dead-letter queue routing after 3 consumer failures
- Default notification templates seeded on first startup

## Port

`8087` (host and container)

## Local Setup

```bash
# Start infrastructure
docker compose up mongodb kafka -d

# Run the service
cd notification-service
mvn spring-boot:run
```

## Environment Variables

| Variable | Default | Description |
|---|---|---|
| `MONGODB_URI` | `mongodb://localhost:27017/notification_db` | MongoDB connection URI |
| `KAFKA_BOOTSTRAP_SERVERS` | `localhost:9092` | Kafka broker address |
| `SMTP_HOST` | `localhost` | SMTP server host |
| `SMTP_PORT` | `1025` | SMTP server port |
| `SMTP_USERNAME` | _(empty)_ | SMTP username |
| `SMTP_PASSWORD` | _(empty)_ | SMTP password |
| `SMTP_AUTH` | `false` | Enable SMTP authentication |
| `SMTP_STARTTLS` | `false` | Enable STARTTLS |
| `NOTIFICATION_SENDER_EMAIL` | `noreply@glpi.local` | Sender email address |
| `WEBHOOK_TIMEOUT_MS` | `5000` | Webhook HTTP timeout in ms |

## REST Endpoints

### Templates
| Method | Path | Description |
|---|---|---|
| `GET` | `/notifications/templates` | List templates (paginated) |
| `POST` | `/notifications/templates` | Create a template |
| `GET` | `/notifications/templates/{id}` | Get template by ID |
| `PUT` | `/notifications/templates/{id}` | Update a template |

### Queue
| Method | Path | Description |
|---|---|---|
| `GET` | `/notifications/queue` | List queued notifications (paginated) |

## Kafka Topics Consumed

| Topic | Consumer Group | Events Handled |
|---|---|---|
| `tickets.events` | `notification-service` | TicketCreated, TicketUpdated, TicketSolved, TicketClosed, TicketDeleted, TicketValidationRequested/Approved/Refused |
| `problems.events` | `notification-service` | ProblemCreated, ProblemSolved |
| `changes.events` | `notification-service` | ChangeCreated, ChangeValidationApproved |
| `sla.events` | `notification-service` | SlaEscalationTriggered |
| `knowledge.events` | `notification-service` | _(reserved for future use)_ |

## Dead-Letter Queues

Failed messages after 3 retries are routed to `{topic}.dlq`.

## Default Seeded Data

On first startup (empty database), `NotificationSeeder` seeds:

- **8 notification templates**: `ticket.created`, `ticket.solved`, `ticket.closed`, `ticket.validation.requested`, `problem.created`, `problem.solved`, `change.created`, `change.validation.approved`

## API Documentation

- OpenAPI spec: `GET /v3/api-docs`
- Swagger UI: `GET /swagger-ui.html`
- Health: `GET /actuator/health`
