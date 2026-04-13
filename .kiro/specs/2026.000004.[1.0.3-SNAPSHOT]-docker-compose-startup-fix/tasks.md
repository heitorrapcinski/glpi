# Tasks — Docker Compose Startup Fix

- [x] 1. Create bugfix branch and bump version to 1.0.3-SNAPSHOT
  - [x] 1.1 Create and checkout branch `bugfix-2026.000004` from current HEAD
  - [x] 1.2 Bump all version references from `1.0.2` to `1.0.3-SNAPSHOT`
  - [x] 1.3 Commit: `chore: bump version to 1.0.3-SNAPSHOT`

- [x] 2. Fix Zookeeper healthcheck and Kafka listener port conflict
  - [x] 2.1 Add `KAFKA_OPTS` env to whitelist `ruok` four-letter word command
  - [x] 2.2 Change Zookeeper healthcheck to `CMD-SHELL` format
  - [x] 2.3 Add explicit `KAFKA_LISTENERS` with separate ports (29092 internal, 9092 host)
  - [x] 2.4 Update `KAFKA_ADVERTISED_LISTENERS` to use `kafka:29092` for internal
  - [x] 2.5 Update all `KAFKA_BOOTSTRAP_SERVERS` defaults from `kafka:9092` to `kafka:29092`
  - [x] 2.6 Update README.md Kafka bootstrap server default
  - [x] 2.7 Remove obsolete `version: "3.9"` key
  - [x] 2.8 Commit: `2026.000004.2: fix zookeeper healthcheck and kafka listener port conflict`

- [x] 3. Fix Spring Boot Maven plugin repackage goal
  - [x] 3.1 Add `<executions>` block with `repackage` goal to `spring-boot-maven-plugin` in parent POM
  - [x] 3.2 Commit: `2026.000004.3: add spring-boot-maven-plugin repackage execution`

- [x] 4. Version control and release
  - [x] Ensure all previous tasks are complete and tests pass
  - [x] Remove SNAPSHOT suffix from all version references in the codebase
  - [x] Commit the version bump: "release: 1.0.3 - docker-compose-startup-fix"
  - [x] Merge branch into main/master
  - [x] Apply Git tag: 1.0.3 (without SNAPSHOT)
  - [x] Push branch, merge, and tag to remote
