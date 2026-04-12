# Bugfix Requirements Document

## Introduction

Running `docker compose up` fails for all 8 microservices because the Docker build context is set to each service's subdirectory (e.g., `./api-gateway`), while every Dockerfile expects to `COPY` files from the workspace root — specifically the parent `pom.xml` and the `common/` module. Docker cannot access files outside the build context, so the build fails immediately with "file not found" errors for every service.

## Bug Analysis

### Current Behavior (Defect)

1.1 WHEN `docker compose up` is executed THEN the system fails to build all 8 microservice images because each service's `build.context` in `docker-compose.yml` points to the service subdirectory (e.g., `context: ./api-gateway`), which overrides the shared anchor's `context: .`

1.2 WHEN Docker processes a service Dockerfile with a subdirectory build context THEN the `COPY pom.xml ./` instruction fails because the root `pom.xml` does not exist inside the service subdirectory

1.3 WHEN Docker processes a service Dockerfile with a subdirectory build context THEN the `COPY common/pom.xml common/` and `COPY common/src common/src/` instructions fail because the `common/` module does not exist inside the service subdirectory

1.4 WHEN Docker processes a service Dockerfile with a subdirectory build context THEN the `COPY <service>/pom.xml <service>/` instruction fails because the service directory is the context root itself, not a child of the workspace root

### Expected Behavior (Correct)

2.1 WHEN `docker compose up` is executed THEN the system SHALL successfully build all 8 microservice images without file-not-found errors

2.2 WHEN Docker processes a service Dockerfile THEN the `COPY pom.xml ./` instruction SHALL succeed by finding the root `pom.xml` within the build context

2.3 WHEN Docker processes a service Dockerfile THEN the `COPY common/pom.xml common/` and `COPY common/src common/src/` instructions SHALL succeed by finding the `common/` module within the build context

2.4 WHEN Docker processes a service Dockerfile THEN the `COPY <service>/pom.xml <service>/` and `COPY <service>/src <service>/src/` instructions SHALL succeed by finding the service source within the build context

### Unchanged Behavior (Regression Prevention)

3.1 WHEN `docker compose up` is executed for infrastructure services (zookeeper, kafka, mongodb) THEN the system SHALL CONTINUE TO start them using their pre-built images without any build step

3.2 WHEN a microservice image is built THEN the system SHALL CONTINUE TO produce a multi-stage image with a Maven build stage and a JRE runtime stage

3.3 WHEN a microservice container starts THEN the system SHALL CONTINUE TO run as the non-root `glpi` user

3.4 WHEN a microservice container starts THEN the system SHALL CONTINUE TO expose the same ports as currently configured (8080-8088)

3.5 WHEN `docker compose --profile seed up` is executed THEN the seeder services SHALL CONTINUE TO use the pre-built service images and run in the correct dependency order

3.6 WHEN the YAML anchor `x-microservice-defaults` is used THEN the system SHALL CONTINUE TO apply shared environment variables (`SPRING_PROFILES_ACTIVE`, `KAFKA_BOOTSTRAP_SERVERS`, `JWT_PUBLIC_KEY`, `JWT_PRIVATE_KEY`) and dependency conditions to all microservices

3.7 WHEN Docker layer caching is used THEN the system SHALL CONTINUE TO benefit from the existing Dockerfile layer ordering (parent POM → common module → service POM → dependencies → service source)

---

## Bug Condition

```pascal
FUNCTION isBugCondition(X)
  INPUT: X of type DockerComposeService
  OUTPUT: boolean

  // Returns true when the service has a per-service build context override
  // that conflicts with its Dockerfile's COPY instructions expecting root-level paths
  RETURN X.build.context ≠ "." AND X.dockerfile.copies_root_level_files = true
END FUNCTION
```

## Fix Checking Property

```pascal
// Property: Fix Checking — All microservice builds succeed
FOR ALL X WHERE isBugCondition(X) DO
  result ← dockerComposeBuild'(X)
  ASSERT result.build_success = true
    AND result.root_pom_accessible = true
    AND result.common_module_accessible = true
    AND result.service_source_accessible = true
END FOR
```

## Preservation Checking Property

```pascal
// Property: Preservation Checking — Non-buggy services and runtime behavior unchanged
FOR ALL X WHERE NOT isBugCondition(X) DO
  ASSERT dockerComposeBuild(X) = dockerComposeBuild'(X)
END FOR
```
