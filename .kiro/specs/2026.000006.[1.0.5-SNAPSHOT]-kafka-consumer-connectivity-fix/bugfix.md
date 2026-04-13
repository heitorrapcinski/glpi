# Bugfix Requirements Document

## Introduction

The `notification-service` and `ticket-service` microservices experience persistent Kafka consumer disconnections when running inside Docker containers. Both services repeatedly log `"Node -1 disconnected"` errors, indicating the Kafka consumers cannot complete the initial bootstrap metadata fetch against the broker. Node ID `-1` is the bootstrap server node, meaning the consumers never establish a working connection.

The root cause is twofold:
1. **Missing Docker Spring profile**: `SPRING_PROFILES_ACTIVE: docker` is set in `docker-compose.yml`, but no `application-docker.yml` exists in either service to override the default `localhost:9092` bootstrap server.
2. **Missing reconnection configuration**: Neither service configures `reconnect.backoff.ms`, `retry.backoff.ms`, `request.timeout.ms`, or `connections.max.idle.ms` in their `KafkaConfig.java`, so transient network failures during container startup cause permanent disconnections with no recovery.

This affects 7 Kafka consumers across 2 services (6 in `notification-service`, 1 in `ticket-service`), blocking all event-driven processing (ticket events, problem events, change events, asset events, SLA events, and knowledge events).

## Bug Analysis

### Current Behavior (Defect)

1.1 WHEN `notification-service` runs inside Docker with `SPRING_PROFILES_ACTIVE: docker` and `KAFKA_BOOTSTRAP_SERVERS: kafka:29092` THEN the Kafka consumers repeatedly log `"Node -1 disconnected"` and fail to consume messages from any topic

1.2 WHEN `ticket-service` runs inside Docker with `SPRING_PROFILES_ACTIVE: docker` and `KAFKA_BOOTSTRAP_SERVERS: kafka:29092` THEN the Kafka consumer repeatedly logs `"Node -1 disconnected"` and fails to consume messages from the `sla.events` topic

1.3 WHEN either service's Kafka consumer experiences a transient network interruption during container startup THEN the consumer does not attempt reconnection with any backoff strategy and remains permanently disconnected

1.4 WHEN `notification-service` or `ticket-service` starts inside Docker and the Kafka broker is not yet fully ready (despite the healthcheck passing) THEN the consumer fails on the first connection attempt and does not retry with appropriate timeouts

### Expected Behavior (Correct)

2.1 WHEN `notification-service` runs inside Docker with `KAFKA_BOOTSTRAP_SERVERS: kafka:29092` THEN the Kafka consumers SHALL successfully connect to the broker at `kafka:29092` and consume messages from all configured topics (tickets, problems, changes, assets, sla, knowledge events)

2.2 WHEN `ticket-service` runs inside Docker with `KAFKA_BOOTSTRAP_SERVERS: kafka:29092` THEN the Kafka consumer SHALL successfully connect to the broker at `kafka:29092` and consume messages from the `sla.events` topic

2.3 WHEN either service's Kafka consumer experiences a transient network interruption THEN the consumer SHALL automatically reconnect using a configured backoff strategy (`reconnect.backoff.ms`, `retry.backoff.ms`)

2.4 WHEN `notification-service` or `ticket-service` starts inside Docker and the Kafka broker is not yet fully ready THEN the consumer SHALL retry the connection with appropriate timeouts (`request.timeout.ms`, `connections.max.idle.ms`) until the broker becomes available

### Unchanged Behavior (Regression Prevention)

3.1 WHEN `notification-service` or `ticket-service` runs outside Docker (local development) with the default bootstrap server `localhost:9092` THEN the system SHALL CONTINUE TO connect to the local Kafka broker successfully

3.2 WHEN `notification-service` receives a message on any configured topic THEN the system SHALL CONTINUE TO deserialize and process the event correctly using the existing JSON deserializer configuration

3.3 WHEN `ticket-service` receives a message on the `sla.events` topic THEN the system SHALL CONTINUE TO deserialize and process the event correctly using the existing JSON deserializer configuration

3.4 WHEN `notification-service` encounters a processing error after 3 retries THEN the system SHALL CONTINUE TO route the failed message to the Dead Letter Queue (DLQ) using the existing exponential backoff (1s, 4s, 16s)

3.5 WHEN the `KAFKA_BOOTSTRAP_SERVERS` environment variable is set THEN the system SHALL CONTINUE TO use its value to override the default bootstrap server address

3.6 WHEN Kafka producers in either service send messages THEN the system SHALL CONTINUE TO serialize and publish events correctly using the existing JSON serializer configuration
