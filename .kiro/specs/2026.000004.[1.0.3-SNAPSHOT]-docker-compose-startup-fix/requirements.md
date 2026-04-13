# Requirements — Docker Compose Startup Fix

## Problem Statement

Running `docker compose up` fails with multiple cascading issues:

1. **Zookeeper healthcheck fails**: The `confluentinc/cp-zookeeper:7.7.1` image only enables the `srvr` four-letter word command by default, but the healthcheck uses `ruok` which is not whitelisted. This causes the container to be marked unhealthy.

2. **Kafka crashes on startup**: Both `PLAINTEXT` and `PLAINTEXT_HOST` listeners are configured to bind to port `9092` inside the container, causing a fatal `IllegalArgumentException`. Kafka requires separate internal ports for each listener.

3. **Microservices crash with "no main manifest attribute"**: The `spring-boot-maven-plugin` is declared in `<pluginManagement>` in the parent POM without an `<executions>` block to trigger the `repackage` goal. Child modules reference the plugin but it never produces an executable fat JAR.

4. **Obsolete `version` key in docker-compose.yml**: The `version: "3.9"` attribute is obsolete in modern Docker Compose and produces a warning.

## Expected Outcome

All infrastructure services (Zookeeper, Kafka, MongoDB) and all microservices start successfully with `docker compose up -d`.
