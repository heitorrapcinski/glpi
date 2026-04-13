# Kafka Consumer Connectivity Fix — Bugfix Design

## Overview

The `notification-service` (6 consumers) and `ticket-service` (1 consumer) fail to connect to the Kafka broker when running inside Docker. The `docker-compose.yml` sets `SPRING_PROFILES_ACTIVE: docker`, but neither service provides an `application-docker.yml` profile file. Although `KAFKA_BOOTSTRAP_SERVERS` is injected as an environment variable and the default `application.yml` already uses `${KAFKA_BOOTSTRAP_SERVERS:localhost:9092}`, the `KafkaConfig.java` in both services reads the bootstrap server via `@Value("${spring.kafka.bootstrap-servers:localhost:9092}")` — which correctly resolves from the YAML property. However, neither service configures Kafka client reconnection properties (`reconnect.backoff.ms`, `retry.backoff.ms`, `request.timeout.ms`, `connections.max.idle.ms`), so any transient failure during container startup causes permanent disconnection with no recovery.

The fix involves two changes:
1. Create `application-docker.yml` in both services to explicitly set the Docker-specific bootstrap server, ensuring the `docker` Spring profile is properly handled.
2. Add reconnection and timeout configuration to both `KafkaConfig.java` files so consumers recover from transient network failures.

## Glossary

- **Bug_Condition (C)**: The condition that triggers the bug — Kafka consumers running inside Docker fail to establish or recover a connection to the broker due to missing profile configuration and absent reconnection parameters.
- **Property (P)**: The desired behavior — consumers connect to `kafka:29092` inside Docker and automatically recover from transient network failures using configured backoff strategies.
- **Preservation**: Existing behavior that must remain unchanged — local development connectivity (`localhost:9092`), message deserialization, DLQ routing, producer serialization, and environment variable overrides.
- **KafkaConfig.java**: The Spring `@Configuration` class in each service that builds `ConsumerFactory` and `ProducerFactory` beans with Kafka client properties.
- **application-docker.yml**: A Spring Boot profile-specific configuration file activated when `SPRING_PROFILES_ACTIVE=docker`, overriding properties from the default `application.yml`.
- **Node -1**: The Kafka bootstrap server node ID used during the initial metadata fetch; repeated disconnection from this node means the consumer never establishes a working connection.
- **DLQ (Dead Letter Queue)**: The error-handling mechanism in `notification-service` that routes failed messages after 3 retries with exponential backoff.

## Bug Details

### Bug Condition

The bug manifests when either service runs inside Docker with `SPRING_PROFILES_ACTIVE: docker`. The `docker` profile is activated but no `application-docker.yml` exists to provide Docker-specific overrides. Additionally, the `KafkaConfig.java` in both services does not configure any reconnection backoff or timeout properties, so the Kafka client cannot recover from transient network failures during container startup.

**Formal Specification:**
```
FUNCTION isBugCondition(input)
  INPUT: input of type ServiceStartupContext
  OUTPUT: boolean

  LET profileActive     = input.springProfilesActive CONTAINS "docker"
  LET dockerYmlMissing  = NOT fileExists("application-docker.yml", input.serviceResourcesPath)
  LET noReconnectConfig = NOT hasProperty(input.kafkaConsumerConfig, "reconnect.backoff.ms")
                          AND NOT hasProperty(input.kafkaConsumerConfig, "retry.backoff.ms")
                          AND NOT hasProperty(input.kafkaConsumerConfig, "request.timeout.ms")
                          AND NOT hasProperty(input.kafkaConsumerConfig, "connections.max.idle.ms")

  RETURN (profileActive AND dockerYmlMissing)
         OR (noReconnectConfig AND input.brokerTemporarilyUnavailable)
END FUNCTION
```

### Examples

- **notification-service in Docker**: Service starts with `SPRING_PROFILES_ACTIVE=docker` and `KAFKA_BOOTSTRAP_SERVERS=kafka:29092`. No `application-docker.yml` exists. Consumers log `"Node -1 disconnected"` repeatedly and never consume from `tickets.events`, `problems.events`, `changes.events`, `assets.events`, `sla.events`, or `knowledge.events`.
- **ticket-service in Docker**: Same startup conditions. The single consumer on `sla.events` logs `"Node -1 disconnected"` and never processes SLA escalation events.
- **Transient broker unavailability**: Kafka broker passes its healthcheck but is not fully ready for client connections. Both services attempt one connection, fail, and never retry because no `reconnect.backoff.ms` or `retry.backoff.ms` is configured.
- **Edge case — local development**: Service runs without the `docker` profile, connects to `localhost:9092` successfully. This scenario is NOT affected by the bug.

## Expected Behavior

### Preservation Requirements

**Unchanged Behaviors:**
- Local development connectivity: services running outside Docker with the default `localhost:9092` bootstrap server must continue to connect successfully.
- Message deserialization: `notification-service` consumers must continue to deserialize JSON events using `JsonDeserializer` with trusted packages `"*"`.
- Message deserialization: `ticket-service` consumer must continue to deserialize JSON events from `sla.events` using `JsonDeserializer` with trusted packages `"*"`.
- DLQ routing: `notification-service` must continue to route failed messages to the Dead Letter Queue after 3 retries with exponential backoff (1s, 4s, 16s).
- Producer serialization: Kafka producers in both services must continue to serialize and publish events using `JsonSerializer`.
- Environment variable override: `KAFKA_BOOTSTRAP_SERVERS` must continue to override the default bootstrap server address when set.

**Scope:**
All inputs that do NOT involve Docker-profile startup or transient broker unavailability should be completely unaffected by this fix. This includes:
- Local development runs (no `docker` profile active)
- Message consumption and processing logic
- DLQ error handling and retry logic
- Producer message publishing
- All non-Kafka-related service functionality

## Hypothesized Root Cause

Based on the bug description and code analysis, the most likely issues are:

1. **Missing `application-docker.yml` profile file**: `docker-compose.yml` sets `SPRING_PROFILES_ACTIVE: docker` for all microservices, but neither `notification-service` nor `ticket-service` has an `application-docker.yml` in `src/main/resources/`. While the environment variable `KAFKA_BOOTSTRAP_SERVERS` is injected and the default `application.yml` uses `${KAFKA_BOOTSTRAP_SERVERS:localhost:9092}`, the absence of the profile file means there is no explicit Docker-specific configuration layer. If the environment variable resolution fails or is not picked up correctly by the `@Value` annotation in `KafkaConfig.java`, the consumer falls back to `localhost:9092` — which is unreachable from inside the Docker network.

2. **Missing reconnection backoff configuration**: Neither `KafkaConfig.java` sets `ConsumerConfig.RECONNECT_BACKOFF_MS_CONFIG` or `ConsumerConfig.RECONNECT_BACKOFF_MAX_MS_CONFIG`. The Kafka client defaults (50ms initial, 1000ms max) may be insufficient for Docker container startup timing, and without explicit configuration the behavior is unpredictable across environments.

3. **Missing retry backoff configuration**: Neither `KafkaConfig.java` sets `ConsumerConfig.RETRY_BACKOFF_MS_CONFIG`. Without this, failed metadata fetch requests are retried with the default 100ms backoff, which can overwhelm the broker during startup and lead to repeated failures.

4. **Missing request timeout and idle connection configuration**: Neither `KafkaConfig.java` sets `ConsumerConfig.REQUEST_TIMEOUT_MS_CONFIG` or `ConsumerConfig.CONNECTIONS_MAX_IDLE_MS_CONFIG`. The default request timeout (30s) may be too short for a broker that is still initializing, and idle connections may be closed prematurely.

## Correctness Properties

Property 1: Bug Condition — Docker Kafka Consumer Connectivity

_For any_ service startup context where the Docker profile is active and the Kafka broker is reachable at `kafka:29092`, the fixed services SHALL successfully establish a consumer connection to the broker and consume messages from all configured topics without logging `"Node -1 disconnected"` errors.

**Validates: Requirements 2.1, 2.2, 2.3, 2.4**

Property 2: Preservation — Local Development and Existing Behavior

_For any_ service startup context where the Docker profile is NOT active (local development), the fixed services SHALL produce the same behavior as the original services, preserving local `localhost:9092` connectivity, JSON deserialization, DLQ routing, producer serialization, and environment variable overrides.

**Validates: Requirements 3.1, 3.2, 3.3, 3.4, 3.5, 3.6**


## Fix Implementation

### Changes Required

Assuming our root cause analysis is correct:

**File**: `notification-service/src/main/resources/application-docker.yml`

**Action**: Create new file

**Specific Changes**:
1. **Create Docker profile configuration**: Add `application-docker.yml` that explicitly sets `spring.kafka.bootstrap-servers` to `${KAFKA_BOOTSTRAP_SERVERS:kafka:29092}` for the Docker environment. This provides a proper Spring profile override layer and ensures the Docker-specific broker address is used even if environment variable injection has issues.

---

**File**: `ticket-service/src/main/resources/application-docker.yml`

**Action**: Create new file

**Specific Changes**:
1. **Create Docker profile configuration**: Add `application-docker.yml` with the same `spring.kafka.bootstrap-servers` override as `notification-service`, targeting `${KAFKA_BOOTSTRAP_SERVERS:kafka:29092}`.

---

**File**: `notification-service/src/main/java/com/glpi/notification/config/KafkaConfig.java`

**Function**: `consumerFactory()`

**Specific Changes**:
1. **Add reconnection backoff**: Set `ConsumerConfig.RECONNECT_BACKOFF_MS_CONFIG` to `1000` (1 second initial backoff) and `ConsumerConfig.RECONNECT_BACKOFF_MAX_MS_CONFIG` to `10000` (10 second max backoff) in the consumer config map.
2. **Add retry backoff**: Set `ConsumerConfig.RETRY_BACKOFF_MS_CONFIG` to `1000` (1 second) to prevent overwhelming the broker during startup.
3. **Add request timeout**: Set `ConsumerConfig.REQUEST_TIMEOUT_MS_CONFIG` to `60000` (60 seconds) to allow sufficient time for broker initialization.
4. **Add idle connection timeout**: Set `ConsumerConfig.CONNECTIONS_MAX_IDLE_MS_CONFIG` to `300000` (5 minutes) to prevent premature connection closure.

---

**File**: `ticket-service/src/main/java/com/glpi/ticket/config/KafkaConfig.java`

**Function**: `consumerFactory()`

**Specific Changes**:
1. **Add reconnection backoff**: Set `ConsumerConfig.RECONNECT_BACKOFF_MS_CONFIG` to `1000` and `ConsumerConfig.RECONNECT_BACKOFF_MAX_MS_CONFIG` to `10000` in the consumer config map.
2. **Add retry backoff**: Set `ConsumerConfig.RETRY_BACKOFF_MS_CONFIG` to `1000`.
3. **Add request timeout**: Set `ConsumerConfig.REQUEST_TIMEOUT_MS_CONFIG` to `60000`.
4. **Add idle connection timeout**: Set `ConsumerConfig.CONNECTIONS_MAX_IDLE_MS_CONFIG` to `300000`.

## Testing Strategy

### Validation Approach

The testing strategy follows a two-phase approach: first, surface counterexamples that demonstrate the bug on unfixed code, then verify the fix works correctly and preserves existing behavior.

### Exploratory Bug Condition Checking

**Goal**: Surface counterexamples that demonstrate the bug BEFORE implementing the fix. Confirm or refute the root cause analysis. If we refute, we will need to re-hypothesize.

**Test Plan**: Run both services inside Docker using `docker compose up` and observe Kafka consumer logs. Check for `"Node -1 disconnected"` errors and verify that no messages are consumed from any topic. Inspect the resolved `spring.kafka.bootstrap-servers` property at runtime to confirm whether it resolves to `localhost:9092` or `kafka:29092`.

**Test Cases**:
1. **notification-service Docker startup**: Start `notification-service` in Docker, observe logs for `"Node -1 disconnected"` on all 6 consumer topics (will fail on unfixed code)
2. **ticket-service Docker startup**: Start `ticket-service` in Docker, observe logs for `"Node -1 disconnected"` on `sla.events` consumer (will fail on unfixed code)
3. **Transient broker restart**: Start services, then restart the Kafka broker container. Observe whether consumers reconnect (will fail on unfixed code — no reconnection backoff configured)
4. **Delayed broker readiness**: Start services before Kafka broker is fully ready (remove healthcheck dependency temporarily). Observe whether consumers eventually connect (will fail on unfixed code)

**Expected Counterexamples**:
- Consumer logs show repeated `"Node -1 disconnected"` with no recovery
- Possible causes: bootstrap server resolving to `localhost:9092` inside Docker, or no reconnection backoff causing permanent failure after first transient error

### Fix Checking

**Goal**: Verify that for all inputs where the bug condition holds, the fixed services produce the expected behavior.

**Pseudocode:**
```
FOR ALL input WHERE isBugCondition(input) DO
  result := startServiceInDocker_fixed(input)
  ASSERT result.consumersConnected = true
  ASSERT result.nodeDisconnectedErrors = 0
  ASSERT result.messagesConsumedFromAllTopics = true
END FOR
```

### Preservation Checking

**Goal**: Verify that for all inputs where the bug condition does NOT hold, the fixed services produce the same result as the original services.

**Pseudocode:**
```
FOR ALL input WHERE NOT isBugCondition(input) DO
  ASSERT startServiceLocal_original(input) = startServiceLocal_fixed(input)
END FOR
```

**Testing Approach**: Property-based testing is recommended for preservation checking because:
- It generates many test cases automatically across the input domain
- It catches edge cases that manual unit tests might miss
- It provides strong guarantees that behavior is unchanged for all non-buggy inputs

**Test Plan**: Observe behavior on UNFIXED code first for local development runs, message deserialization, DLQ routing, and producer serialization, then write property-based tests capturing that behavior.

**Test Cases**:
1. **Local development connectivity**: Verify that running services without the `docker` profile still connects to `localhost:9092` successfully after the fix
2. **Message deserialization preservation**: Verify that JSON deserialization with trusted packages `"*"` continues to work for all consumer topics
3. **DLQ routing preservation**: Verify that `notification-service` DLQ routing (3 retries, exponential backoff 1s/4s/16s) continues to work after the fix
4. **Producer serialization preservation**: Verify that Kafka producers in both services continue to serialize and publish events correctly
5. **Environment variable override preservation**: Verify that setting `KAFKA_BOOTSTRAP_SERVERS` continues to override the default bootstrap server

### Unit Tests

- Test that `KafkaConfig.consumerFactory()` includes reconnection backoff properties in the consumer config map
- Test that `KafkaConfig.consumerFactory()` includes retry backoff and request timeout properties
- Test that `application-docker.yml` is loaded when the `docker` profile is active and overrides the bootstrap server
- Test that the default `application.yml` is used when no profile is active (local development)

### Property-Based Tests

- Generate random combinations of Spring profiles and environment variables, verify that the correct bootstrap server is resolved in each case
- Generate random Kafka consumer configurations with and without reconnection properties, verify that the consumer factory always includes the required reconnection parameters after the fix
- Generate random service startup sequences with varying broker readiness delays, verify that consumers eventually connect when reconnection backoff is configured

### Integration Tests

- Start both services in Docker with `docker compose up` and verify all 7 consumers connect and consume messages
- Restart the Kafka broker while services are running and verify consumers reconnect within the configured backoff window
- Run services locally without the `docker` profile and verify they connect to `localhost:9092` as before
- Publish test events to all topics and verify end-to-end consumption in both Docker and local environments
