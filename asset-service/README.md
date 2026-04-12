# GLPI Asset Service

CMDB asset and license management microservice for the GLPI Microservices Backend.

## Purpose

Manages Configuration Items (CIs) in the CMDB including computers, network equipment, monitors, printers, phones, peripherals, software, and software licenses. Supports polymorphic asset types, hierarchical locations, configurable asset states, and license compliance tracking.

## Port

- **Internal**: 8085
- **Host**: 8085

## Endpoints

| Method | Path | Description |
|--------|------|-------------|
| GET | `/assets/{type}` | List assets by type (paginated) |
| POST | `/assets/{type}` | Create a new asset |
| GET | `/assets/{type}/{id}` | Get asset by ID |
| PUT | `/assets/{type}/{id}` | Update an asset |
| DELETE | `/assets/{type}/{id}` | Soft-delete an asset |
| GET | `/assets/{type}/{id}/networkports` | List network ports |
| POST | `/assets/{type}/{id}/networkports` | Add a network port |
| GET | `/assets/computers/{id}/software` | List software installations |
| GET | `/assets/computers/{id}/devices` | List hardware devices |
| GET | `/assets/{type}/{id}/tickets` | List linked tickets |
| GET | `/assets/licenses` | List software licenses |
| POST | `/assets/licenses` | Create a license |
| GET | `/assets/licenses/{id}` | Get license by ID |
| GET | `/assets/licenses/{id}/compliance` | Check license compliance |
| GET | `/assets/states` | List asset states |
| POST | `/assets/states` | Create an asset state |
| GET | `/assets/locations` | List locations |
| POST | `/assets/locations` | Create a location |

## Kafka Topics

- **Produces**: `assets.events` (AssetCreated, AssetUpdated, AssetDeleted, LicenseOverused)

## Environment Variables

| Variable | Default | Description |
|----------|---------|-------------|
| `MONGODB_URI` | `mongodb://localhost:27017/asset_db` | MongoDB connection URI |
| `KAFKA_BOOTSTRAP_SERVERS` | `localhost:9092` | Kafka broker addresses |

## Local Setup

```bash
mvn -pl common,asset-service -am spring-boot:run -pl asset-service
```

## API Documentation

- OpenAPI spec: `http://localhost:8085/v3/api-docs`
- Swagger UI: `http://localhost:8085/swagger-ui.html`
