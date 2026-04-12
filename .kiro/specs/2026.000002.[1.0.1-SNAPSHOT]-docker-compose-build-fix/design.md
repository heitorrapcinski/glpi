# Docker Compose Build Context Bugfix Design

## Overview

All 8 microservice builds fail because each service definition in `docker-compose.yml` overrides the shared YAML anchor's `build.context: .` with a per-service `build.context: ./<service>`. Since every Dockerfile expects root-level paths (`COPY pom.xml ./`, `COPY common/...`, `COPY <service>/...`), Docker cannot resolve those paths within the subdirectory context.

The fix is confined to `docker-compose.yml`: remove the per-service `context` overrides so the anchor's root context (`.`) is inherited, and add a `dockerfile` key pointing to each service's Dockerfile (e.g., `dockerfile: api-gateway/Dockerfile`). No Dockerfile changes are needed.

## Glossary

- **Bug_Condition (C)**: A microservice definition in `docker-compose.yml` whose `build.context` resolves to a subdirectory instead of the workspace root, causing Dockerfile `COPY` instructions to fail
- **Property (P)**: Every microservice's `build.context` resolves to `.` (workspace root) and `build.dockerfile` points to `<service>/Dockerfile`, so all `COPY` instructions succeed
- **Preservation**: Infrastructure services (zookeeper, kafka, mongodb), seeder services, shared anchor defaults, port mappings, Dockerfile content, and multi-stage build structure must remain unchanged
- **x-microservice-defaults**: YAML anchor providing shared `build`, `environment`, and `depends_on` configuration to all microservices
- **build.context**: Docker Compose key that sets the root directory sent to the Docker daemon as the build context
- **build.dockerfile**: Docker Compose key that specifies the path to the Dockerfile relative to the build context

## Bug Details

### Bug Condition

The bug manifests when `docker compose build` (or `docker compose up`) is executed. Each of the 8 microservice definitions includes a `build:` block with `context: ./<service>`, which completely replaces the anchor's `build:` block (YAML merge semantics replace entire mapping keys). This makes the service subdirectory the build context root, so Dockerfile instructions like `COPY pom.xml ./` and `COPY common/pom.xml common/` fail because those files exist only at the workspace root.

**Formal Specification:**
```
FUNCTION isBugCondition(X)
  INPUT: X of type DockerComposeServiceDefinition
  OUTPUT: boolean

  RETURN X.build.context ≠ "."
         AND X.hasDockerfile = true
         AND X.dockerfile.referencesRootLevelPaths = true
END FUNCTION
```

### Examples

- `api-gateway`: `build.context: ./api-gateway` → `COPY pom.xml ./` fails (root `pom.xml` not in `./api-gateway/`)
- `identity-service`: `build.context: ./identity-service` → `COPY common/pom.xml common/` fails (`common/` not in `./identity-service/`)
- `ticket-service`: `build.context: ./ticket-service` → `COPY ticket-service/pom.xml ticket-service/` fails (path would be `./ticket-service/ticket-service/pom.xml`)
- `knowledge-service`: `build.context: ./knowledge-service` → same pattern, all 4 root-level COPY instructions fail

## Expected Behavior

### Preservation Requirements

**Unchanged Behaviors:**
- Infrastructure services (zookeeper, kafka, mongodb) must continue to start using their pre-built images with no build step
- Seeder services must continue to use pre-built service images and run in the correct dependency order
- The `x-microservice-defaults` anchor must continue to provide shared `environment` variables (`SPRING_PROFILES_ACTIVE`, `KAFKA_BOOTSTRAP_SERVERS`, `JWT_PUBLIC_KEY`, `JWT_PRIVATE_KEY`) and `depends_on` conditions
- All microservice port mappings (8080–8088) must remain unchanged
- All Dockerfiles must remain unmodified — multi-stage build structure, layer caching order, non-root `glpi` user, and exposed ports are preserved
- Container names, image names, volume mounts, and network configuration must remain unchanged

**Scope:**
All inputs that do NOT involve the `build.context` and `build.dockerfile` keys of the 8 microservice definitions should be completely unaffected by this fix. This includes:
- Infrastructure service definitions
- Seeder service definitions
- Environment variable mappings
- Port mappings
- Volume and network configuration
- Dockerfile content

## Hypothesized Root Cause

Based on the bug analysis, the root cause is:

1. **YAML Merge Key Override Semantics**: The `<<: *microservice-defaults` merge provides `build: { context: . }` from the anchor. However, each service definition also declares its own `build:` mapping with `context: ./<service>`. Per YAML merge semantics, the local `build:` key entirely replaces the anchor's `build:` key — it does not deep-merge individual sub-keys. This means the anchor's `context: .` is discarded.

2. **Missing `dockerfile` Key**: Even if the context were correct, there is no `dockerfile` key to tell Docker where to find each service's Dockerfile relative to the root context. Without it, Docker defaults to looking for `Dockerfile` at the context root (`.`), which would pick up the wrong file or fail if no root-level Dockerfile exists.

3. **Consistent Pattern Across All 8 Services**: Every microservice definition repeats the same override pattern (`build: context: ./<service>`), confirming this is a systematic configuration error rather than an isolated typo.

## Correctness Properties

Property 1: Bug Condition — All Microservice Build Contexts Resolve to Root

_For any_ microservice definition in `docker-compose.yml` where the service has a Dockerfile that references root-level paths (pom.xml, common/, <service>/), the resolved `build.context` SHALL be `.` (workspace root) and `build.dockerfile` SHALL be `<service>/Dockerfile`, ensuring all `COPY` instructions can resolve their source paths.

**Validates: Requirements 2.1, 2.2, 2.3, 2.4**

Property 2: Preservation — Non-Build Configuration Unchanged

_For any_ configuration in `docker-compose.yml` that is NOT the `build.context` or `build.dockerfile` of a microservice definition, the fixed file SHALL produce the same resolved configuration as the original file, preserving infrastructure services, seeder services, port mappings, environment variables, volumes, networks, and container names.

**Validates: Requirements 3.1, 3.2, 3.3, 3.4, 3.5, 3.6, 3.7**

## Fix Implementation

### Changes Required

**File**: `docker-compose.yml`

**Scope**: Only the `build:` block of each of the 8 microservice service definitions.

**Specific Changes**:

1. **Keep the anchor's root context**: The `x-microservice-defaults` anchor already defines `build: { context: . }`. This is correct and must remain unchanged.

2. **Replace per-service `build.context` with `build.dockerfile`**: For each of the 8 microservices, change:
   ```yaml
   # BEFORE (buggy)
   build:
     context: ./<service>
   ```
   to:
   ```yaml
   # AFTER (fixed)
   build:
     context: .
     dockerfile: <service>/Dockerfile
   ```
   This preserves the root context from the anchor and adds the `dockerfile` key to locate each service's Dockerfile.

3. **Affected services** (all 8):
   - `api-gateway` → `dockerfile: api-gateway/Dockerfile`
   - `identity-service` → `dockerfile: identity-service/Dockerfile`
   - `ticket-service` → `dockerfile: ticket-service/Dockerfile`
   - `problem-service` → `dockerfile: problem-service/Dockerfile`
   - `change-service` → `dockerfile: change-service/Dockerfile`
   - `asset-service` → `dockerfile: asset-service/Dockerfile`
   - `sla-service` → `dockerfile: sla-service/Dockerfile`
   - `notification-service` → `dockerfile: notification-service/Dockerfile`
   - `knowledge-service` → `dockerfile: knowledge-service/Dockerfile`

4. **No Dockerfile changes**: All 8 Dockerfiles already use root-relative paths (`COPY pom.xml ./`, `COPY common/...`, `COPY <service>/...`) and require no modification.

5. **No infrastructure or seeder changes**: Zookeeper, kafka, mongodb, and all seeder services are image-based and have no `build` block to modify.

## Testing Strategy

### Validation Approach

The testing strategy follows a two-phase approach: first, surface counterexamples that demonstrate the bug on unfixed code, then verify the fix works correctly and preserves existing behavior.

### Exploratory Bug Condition Checking

**Goal**: Surface counterexamples that demonstrate the bug BEFORE implementing the fix. Confirm or refute the root cause analysis. If we refute, we will need to re-hypothesize.

**Test Plan**: Parse the unfixed `docker-compose.yml` and verify that each microservice's resolved `build.context` is a subdirectory, not the root. Optionally run `docker compose config` to see the resolved configuration and confirm the context override.

**Test Cases**:
1. **Context Override Test**: Parse `docker-compose.yml` and assert each microservice's `build.context` is NOT `.` (will fail on unfixed code — confirms the bug)
2. **Dockerfile Path Test**: Assert no `dockerfile` key exists in any microservice's `build` block (will fail on unfixed code — confirms missing path)
3. **Dry-Run Build Test**: Run `docker compose build --dry-run` (if supported) and observe file-not-found errors for `pom.xml` and `common/` (will fail on unfixed code)
4. **Anchor Merge Test**: Verify that the anchor's `build.context: .` is overridden by per-service `build.context` (confirms YAML merge semantics as root cause)

**Expected Counterexamples**:
- All 8 microservices resolve `build.context` to `./<service>` instead of `.`
- Possible causes confirmed: YAML merge key replaces entire `build:` mapping, not individual sub-keys

### Fix Checking

**Goal**: Verify that for all inputs where the bug condition holds, the fixed configuration produces the expected behavior.

**Pseudocode:**
```
FOR ALL service WHERE isBugCondition(service) DO
  resolved := dockerComposeConfig'(service)
  ASSERT resolved.build.context = "."
  ASSERT resolved.build.dockerfile = service.name + "/Dockerfile"
  ASSERT fileExists(resolved.build.context + "/" + "pom.xml")
  ASSERT fileExists(resolved.build.context + "/" + "common/pom.xml")
  ASSERT fileExists(resolved.build.context + "/" + service.name + "/Dockerfile")
END FOR
```

### Preservation Checking

**Goal**: Verify that for all inputs where the bug condition does NOT hold, the fixed file produces the same result as the original file.

**Pseudocode:**
```
FOR ALL config_key WHERE NOT isBugCondition(config_key) DO
  ASSERT dockerComposeConfig(config_key) = dockerComposeConfig'(config_key)
END FOR
```

**Testing Approach**: Property-based testing is recommended for preservation checking because:
- It can generate arbitrary service name / config key combinations and verify they are unchanged
- It catches edge cases like seeder services accidentally inheriting build changes
- It provides strong guarantees that non-build configuration is identical before and after the fix

**Test Plan**: Capture the resolved `docker compose config` output for all non-build keys before the fix, then verify the same output after the fix.

**Test Cases**:
1. **Infrastructure Preservation**: Verify zookeeper, kafka, mongodb definitions are byte-identical before and after the fix
2. **Seeder Preservation**: Verify all 6 seeder service definitions are byte-identical before and after the fix
3. **Port Mapping Preservation**: Verify all microservice port mappings (8080–8088) are unchanged
4. **Environment Variable Preservation**: Verify all environment variable blocks are unchanged
5. **Anchor Preservation**: Verify `x-microservice-defaults` shared environment and depends_on are unchanged

### Unit Tests

- Parse `docker-compose.yml` and verify each microservice's `build.context` is `.`
- Parse `docker-compose.yml` and verify each microservice's `build.dockerfile` is `<service>/Dockerfile`
- Verify no Dockerfile content has changed (checksum comparison)
- Verify infrastructure services have no `build` key

### Property-Based Tests

- Generate random service names from the set of 8 microservices and verify `build.context` is always `.` and `build.dockerfile` is always `<service>/Dockerfile`
- Generate random config keys from non-build sections and verify they are identical before and after the fix
- Generate random seeder/infrastructure service names and verify their definitions are unchanged

### Integration Tests

- Run `docker compose config` and verify the resolved output has correct build contexts for all services
- Run `docker compose build` for a single service (e.g., `api-gateway`) and verify it succeeds without file-not-found errors
- Run `docker compose build` for all services and verify all 8 images are built successfully
