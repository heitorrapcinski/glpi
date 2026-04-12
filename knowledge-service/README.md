# GLPI Knowledge Service

Knowledge base article and category management microservice for the GLPI backend.

## Purpose

Manages knowledge base articles with visibility rules, FAQ flags, revision history, comments, item linking, and full-text search. Supports hierarchical categories.

## Port

- **Internal port**: 8088
- **Host port**: 8088

## Endpoints

| Method | Path | Description |
|--------|------|-------------|
| GET | `/knowledge/articles` | List articles (paginated, visibility-filtered) |
| POST | `/knowledge/articles` | Create a new article |
| GET | `/knowledge/articles/{id}` | Get article by ID (increments view counter) |
| PUT | `/knowledge/articles/{id}` | Update an article |
| DELETE | `/knowledge/articles/{id}` | Delete an article |
| GET | `/knowledge/articles/{id}/revisions` | Get article revision history |
| POST | `/knowledge/articles/{id}/comments` | Add a comment to an article |
| POST | `/knowledge/articles/{id}/links` | Link article to an ITIL item |
| GET | `/knowledge/articles/search?q={query}` | Full-text search |
| GET | `/knowledge/categories` | List categories (paginated) |
| POST | `/knowledge/categories` | Create a new category |
| GET | `/knowledge/categories/{id}` | Get category by ID |

## Kafka Topics

| Topic | Direction | Events |
|-------|-----------|--------|
| `knowledge.events` | Produces | `KnowledgeArticleCreated`, `KnowledgeArticleUpdated` |

## Environment Variables

| Variable | Default | Description |
|----------|---------|-------------|
| `MONGODB_URI` | `mongodb://localhost:27017/knowledge_db` | MongoDB connection URI |
| `KAFKA_BOOTSTRAP_SERVERS` | `localhost:9092` | Kafka broker addresses |

## Local Setup

```bash
# From project root
mvn -pl common,knowledge-service -am package -DskipTests
java -jar knowledge-service/target/knowledge-service-1.0.1.jar
```

## API Documentation

- OpenAPI spec: `http://localhost:8088/v3/api-docs`
- Swagger UI: `http://localhost:8088/swagger-ui.html`
