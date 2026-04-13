# Dockerfile Maven Reactor Fix Design

## Overview

All 8 microservice Docker builds fail because each Dockerfile copies only the parent `pom.xml`, the `common/` module POM + source, and its own service POM into the container. The parent `pom.xml` declares 10 modules in its `<modules>` section. Maven validates all declared modules exist before executing any build, even when `-pl` (project list) is used to filter the build to specific modules. Since 8 of the 10 module directories are missing, Maven fails with "Child module does not exist" errors.

## Glossary

- **Maven Reactor**: Maven's mechanism for building multi-module projects; it reads the parent POM, discovers all modules, validates their existence, then determines build order
- **`-pl` flag**: Maven's "project list" flag that filters which modules to build, but does NOT prevent reactor validation of all declared modules
- **`dependency:go-offline`**: Maven goal that downloads all dependencies for offline use
- **Module POM stub**: A `pom.xml` file copied into the container solely to satisfy Maven's reactor validation, without its corresponding source code

## Bug Details

### Root Cause

Maven's reactor validation is strict: when it reads a parent POM with `<modules>`, it requires all declared module directories to exist and contain a valid `pom.xml`, even when `-pl` is used to build only a subset. Each Dockerfile currently copies only 2 of the 10 modules (common + the target service), leaving 8 modules missing.

### Error Output

```
[ERROR] Child module /workspace/api-gateway of /workspace/pom.xml does not exist
[ERROR] Child module /workspace/identity-service of /workspace/pom.xml does not exist
[ERROR] Child module /workspace/ticket-service of /workspace/pom.xml does not exist
... (8 missing modules per service build)
```

## Fix Implementation

### Strategy

Copy all 10 module `pom.xml` files into each Dockerfile's build stage. This satisfies Maven's reactor validation while keeping the Docker layer cache efficient — only POM files are copied initially, and only the target service's source code is copied later.

### Changes Required

For each of the 8 microservice Dockerfiles, add `COPY` instructions for all sibling module `pom.xml` files that are not already copied. The modules are:

1. `common` (already copied in all Dockerfiles)
2. `api-gateway`
3. `identity-service`
4. `ticket-service`
5. `problem-service`
6. `change-service`
7. `asset-service`
8. `sla-service`
9. `notification-service`
10. `knowledge-service`

Each Dockerfile already copies `common/pom.xml` and its own service `pom.xml`. The fix adds the remaining 8 module POMs.

### Example (knowledge-service/Dockerfile)

Before:
```dockerfile
COPY pom.xml ./
COPY common/pom.xml common/
COPY common/src common/src/
COPY knowledge-service/pom.xml knowledge-service/
```

After:
```dockerfile
COPY pom.xml ./
COPY common/pom.xml common/
COPY api-gateway/pom.xml api-gateway/
COPY identity-service/pom.xml identity-service/
COPY ticket-service/pom.xml ticket-service/
COPY problem-service/pom.xml problem-service/
COPY change-service/pom.xml change-service/
COPY asset-service/pom.xml asset-service/
COPY sla-service/pom.xml sla-service/
COPY notification-service/pom.xml notification-service/
COPY knowledge-service/pom.xml knowledge-service/
COPY common/src common/src/
```

### Key Design Decisions

1. **Copy all module POMs, not just the needed ones**: This is future-proof — if a service adds a dependency on another module, the Dockerfile doesn't need to change.

2. **Copy POMs before source code**: This preserves Docker layer caching. POM files change rarely, so the dependency download layer is cached effectively.

3. **Move `common/src` copy after all POMs**: Group all POM copies together, then copy source. This improves layer cache hit rates.

4. **No changes to `docker-compose.yml`**: The build context fix from spec 2026.000002 is correct and remains unchanged.

5. **No changes to `pom.xml` files**: The parent POM's module declarations are correct and should not be modified.

## Correctness Properties

Property 1: All module POMs are present in the Docker build context before Maven runs.

Property 2: Maven reactor validation succeeds for all 8 microservice builds.

Property 3: No files outside the Dockerfiles are modified.

## Testing Strategy

1. Run `docker compose build` and verify all 8 images build successfully
2. Verify no changes to `docker-compose.yml`, `pom.xml`, or any source files
3. Verify each built image contains the correct JAR file
