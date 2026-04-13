# Tasks — Dockerfile Maven Reactor Fix

- [x] 1. Create bugfix branch and bump version to 1.0.2-SNAPSHOT
  - [x] 1.1 Create and checkout branch `bugfix-2026.000003` from current HEAD
  - [x] 1.2 Bump all version references in the codebase from `1.0.1` to `1.0.2-SNAPSHOT` (root pom.xml, all module pom.xml files, docker-compose.yml `${VERSION:-...}` defaults)
  - [x] 1.3 Commit: `chore: bump version to 1.0.2-SNAPSHOT`

- [x] 2. Fix all microservice Dockerfiles to copy all module POMs
  - [x] 2.1 Update `api-gateway/Dockerfile` to copy all 10 module pom.xml files before running Maven
  - [x] 2.2 Update `identity-service/Dockerfile` to copy all 10 module pom.xml files before running Maven
  - [x] 2.3 Update `ticket-service/Dockerfile` to copy all 10 module pom.xml files before running Maven
  - [x] 2.4 Update `problem-service/Dockerfile` to copy all 10 module pom.xml files before running Maven
  - [x] 2.5 Update `change-service/Dockerfile` to copy all 10 module pom.xml files before running Maven
  - [x] 2.6 Update `asset-service/Dockerfile` to copy all 10 module pom.xml files before running Maven
  - [x] 2.7 Update `sla-service/Dockerfile` to copy all 10 module pom.xml files before running Maven
  - [x] 2.8 Update `notification-service/Dockerfile` to copy all 10 module pom.xml files before running Maven
  - [x] 2.9 Update `knowledge-service/Dockerfile` to copy all 10 module pom.xml files before running Maven
  - [x] 2.10 Verify no changes to docker-compose.yml, pom.xml files, or source code
  - [x] 2.11 Commit: `2026.000003.2: copy all module poms in dockerfiles for maven reactor validation`

- [x] 3. Validate the fix by building all microservice images
  - [x] 3.1 Run `docker compose build` and verify all 8 microservice images build successfully
  - [x] 3.2 Commit: `2026.000003.3: validate dockerfile maven reactor fix`

- [x] 4. Version control and release
  - [x] 4.1 Ensure all previous tasks are complete and tests pass
  - [x] 4.2 Remove SNAPSHOT suffix from all version references in the codebase
  - [x] 4.3 Commit the version bump: "release: 1.0.2 - dockerfile-maven-reactor-fix"
  - [x] 4.4 Merge branch into main/master
  - [x] 4.5 Apply Git tag: 1.0.2 (without SNAPSHOT)
  - [x] 4.6 Push branch, merge, and tag to remote
