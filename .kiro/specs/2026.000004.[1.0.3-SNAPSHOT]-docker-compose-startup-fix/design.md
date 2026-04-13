# Design — Docker Compose Startup Fix

## Fix 1: Zookeeper Healthcheck

- Add `KAFKA_OPTS: "-Dzookeeper.4lw.commands.whitelist=ruok,srvr,stat"` to whitelist the `ruok` command
- Change healthcheck from `["CMD", "bash", "-c", ...]` to `["CMD-SHELL", ...]` for piped commands

## Fix 2: Kafka Listener Port Conflict

- Add explicit `KAFKA_LISTENERS` with separate ports: `PLAINTEXT://0.0.0.0:29092` (internal) and `PLAINTEXT_HOST://0.0.0.0:9092` (host)
- Update `KAFKA_ADVERTISED_LISTENERS` to use `kafka:29092` for internal and `localhost:9092` for host
- Update all `KAFKA_BOOTSTRAP_SERVERS` defaults from `kafka:9092` to `kafka:29092` across microservices and seeders

## Fix 3: Spring Boot Maven Plugin Repackage

- Add `<executions>` block with `repackage` goal to the `spring-boot-maven-plugin` in the parent POM `<pluginManagement>` section
- This ensures all child modules that reference the plugin produce executable fat JARs

## Fix 4: Remove Obsolete Version Key

- Remove `version: "3.9"` from `docker-compose.yml`

## Files Changed

- `docker-compose.yml` — Zookeeper env/healthcheck, Kafka listeners, bootstrap servers, remove version key
- `README.md` — Update Kafka bootstrap server default
- `pom.xml` — Add repackage execution to spring-boot-maven-plugin
