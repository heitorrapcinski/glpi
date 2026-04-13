# Bugfix Requirements Document

## Introduction

Running `docker compose up` (or `docker compose build`) fails for all 8 microservices because each Dockerfile copies only the parent `pom.xml`, the `common/` module, and its own service module into the Docker build context. However, the parent `pom.xml` declares all 10 modules in its `<modules>` section. When Maven runs `dependency:go-offline` or `package`, it reads the parent POM, discovers all 10 modules, and fails because the other 8 module directories do not exist inside the container.

## Bug Analysis

### Current Behavior (Defect)

1.1 WHEN Docker builds any microservice image THEN the `RUN mvn -pl common,<service> dependency:go-offline -q` step fails with exit code 1 because Maven cannot find the 8 missing module directories declared in the parent `pom.xml`

1.2 WHEN Maven reads the parent `pom.xml` inside the Docker container THEN it validates all 10 declared `<module>` entries and fails with "Child module /workspace/<module> of /workspace/pom.xml does not exist" for every module whose `pom.xml` was not copied into the container

1.3 WHEN the `-q` (quiet) flag is used in the Maven command THEN the actual error messages are suppressed, making the root cause difficult to diagnose from the Docker build output alone

### Expected Behavior (Correct)

2.1 WHEN Docker builds any microservice image THEN the `RUN mvn -pl common,<service> dependency:go-offline -q` step SHALL succeed because all module `pom.xml` files are present in the container, allowing Maven to resolve the full reactor

2.2 WHEN Maven reads the parent `pom.xml` inside the Docker container THEN it SHALL find all 10 module `pom.xml` files and successfully validate the reactor before executing the `-pl` filtered build

2.3 WHEN `docker compose build` is executed THEN all 8 microservice images SHALL be built successfully

### Unchanged Behavior (Regression Prevention)

3.1 WHEN infrastructure services (zookeeper, kafka, mongodb) are started THEN the system SHALL CONTINUE TO use their pre-built images without any build step

3.2 WHEN a microservice image is built THEN the system SHALL CONTINUE TO produce a multi-stage image with a Maven build stage and a JRE runtime stage

3.3 WHEN a microservice container starts THEN the system SHALL CONTINUE TO run as the non-root `glpi` user

3.4 WHEN a microservice container starts THEN the system SHALL CONTINUE TO expose the same ports as currently configured (8080-8088)

3.5 WHEN Docker layer caching is used THEN the system SHALL CONTINUE TO benefit from the existing Dockerfile layer ordering (parent POM → module POMs → common source → dependencies → service source)

3.6 WHEN `docker compose --profile seed up` is executed THEN the seeder services SHALL CONTINUE TO use the pre-built service images and run in the correct dependency order

3.7 WHEN the `docker-compose.yml` is processed THEN the build context (`.`) and dockerfile paths SHALL remain unchanged from the previous bugfix (spec 2026.000002)

---

## Bug Condition

```pascal
FUNCTION isBugCondition(X)
  INPUT: X of type Dockerfile
  OUTPUT: boolean

  // Returns true when the Dockerfile copies only a subset of module pom.xml files
  // but the parent pom.xml declares more modules than are copied
  RETURN X.copiedModulePoms.count < X.parentPom.declaredModules.count
END FUNCTION
```

## Fix Checking Property

```pascal
FOR ALL X WHERE isBugCondition(X) DO
  result ← dockerBuild'(X)
  ASSERT result.build_success = true
    AND result.maven_reactor_valid = true
    AND result.all_module_poms_present = true
END FOR
```

## Preservation Checking Property

```pascal
FOR ALL X WHERE NOT isBugCondition(X) DO
  ASSERT dockerBuild(X) = dockerBuild'(X)
END FOR
```
