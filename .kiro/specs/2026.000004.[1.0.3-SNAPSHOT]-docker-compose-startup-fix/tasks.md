# Tasks — Docker Compose Startup Fix

- [x] 1. Create bugfix branch and bump version to 1.0.3-SNAPSHOT
  - [x] 1.1 Create and checkout branch `bugfix-2026.000004` from current HEAD
  - [x] 1.2 Bump all version references from `1.0.2` to `1.0.3-SNAPSHOT`
  - [x] 1.3 Commit: `chore: bump version to 1.0.3-SNAPSHOT`

- [ ] 2. Fix Zookeeper healthcheck and Kafka listener port conflict
  - [ ] 2.1 Add `KAFKA_OPTS` env to whitelist `ruok` four-letter word command
  - [ ] 2.2 Change Zookeeper healthcheck to `CMD-SHELL` format
  - [ ] 2.3 Add explicit `KAFKA_LISTENERS` with separate ports (29092 internal, 9092 host)
  - [ ] 2.4 Update `KAFKA_ADVERTISED_LISTENERS` to use `kafka:29092` for internal
  - [ ] 2.5 Update all `KAFKA_BOOTSTRAP_SERVERS` defaults from `kafka:9092` to `kafka:29092`
  - [ ] 2.6 Update README.md Kafka bootstrap server default
  - [ ] 2.7 Remove obsolete `version: "3.9"` key
  - [ ] 2.8 Commit: `2026.000004.2: fix zookeeper healthcheck and kafka listener port conflict`

- [ ] 3. Fix Spring Boot Maven plugin repackage goal
  - [ ] 3.1 Add `<executions>` block with `repackage` goal to `spring-boot-maven-plugin` in parent POM
  - [ ] 3.2 Commit: `2026.000004.3: add spring-boot-maven-plugin repackage execution`

- [ ] 4. Version control and release
  - [ ] Ensure all previous tasks are complete and tests pass
  - [ ] Remove SNAPSHOT suffix from all version references in the codebase
  - [ ] Commit the version bump: "release: 1.0.3 - docker-compose-startup-fix"
  - [ ] Merge branch into main/master
  - [ ] Apply Git tag: 1.0.3 (without SNAPSHOT)
  - [ ] Push branch, merge, and tag to remote
