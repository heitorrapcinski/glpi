# Tasks — Docker Compose Build Context Bugfix

- [x] 1. Create bugfix branch and bump version to 1.0.1-SNAPSHOT
  - [x] 1.1 Create and checkout branch `bugfix-2026.000002` from current HEAD
  - [x] 1.2 Bump all version references in the codebase from `1.0.0` to `1.0.1-SNAPSHOT` (pom.xml files, docker-compose.yml `${VERSION:-1.0.0}` defaults, etc.)
  - [x] 1.3 Commit: `chore: bump version to 1.0.1-SNAPSHOT`

- [x] 2. Fix microservice build contexts in docker-compose.yml
  - [x] 2.1 Replace `build: context: ./api-gateway` with `build: context: . dockerfile: api-gateway/Dockerfile` for the api-gateway service
  - [x] 2.2 Replace `build: context: ./identity-service` with `build: context: . dockerfile: identity-service/Dockerfile` for the identity-service service
  - [x] 2.3 Replace `build: context: ./ticket-service` with `build: context: . dockerfile: ticket-service/Dockerfile` for the ticket-service service
  - [x] 2.4 Replace `build: context: ./problem-service` with `build: context: . dockerfile: problem-service/Dockerfile` for the problem-service service
  - [x] 2.5 Replace `build: context: ./change-service` with `build: context: . dockerfile: change-service/Dockerfile` for the change-service service
  - [x] 2.6 Replace `build: context: ./asset-service` with `build: context: . dockerfile: asset-service/Dockerfile` for the asset-service service
  - [x] 2.7 Replace `build: context: ./sla-service` with `build: context: . dockerfile: sla-service/Dockerfile` for the sla-service service
  - [x] 2.8 Replace `build: context: ./notification-service` with `build: context: . dockerfile: notification-service/Dockerfile` for the notification-service service
  - [x] 2.9 Replace `build: context: ./knowledge-service` with `build: context: . dockerfile: knowledge-service/Dockerfile` for the knowledge-service service
  - [x] 2.10 Verify no Dockerfiles were modified (all 8 must remain unchanged)
  - [x] 2.11 Verify infrastructure services (zookeeper, kafka, mongodb) and seeder services are unchanged
  - [x] 2.12 Commit: `2026.000002.2: fix microservice build contexts to use root directory`

- [-] 3. Validate the fix
  - [-] 3.1 Run `docker compose config` and verify all 8 microservices resolve `build.context` to `.` and `build.dockerfile` to `<service>/Dockerfile`
  - [~] 3.2 Verify port mappings, environment variables, and depends_on are preserved in the resolved config
  - [~] 3.3 Commit: `2026.000002.3: validate docker-compose build context fix`

- [ ] 4. Version control and release
  - [~] 4.1 Ensure all previous tasks are complete and tests pass
  - [~] 4.2 Remove SNAPSHOT suffix from all version references in the codebase
  - [~] 4.3 Commit the version bump: "release: 1.0.1 - docker-compose-build-fix"
  - [~] 4.4 Merge branch into main/master
  - [~] 4.5 Apply Git tag: 1.0.1 (without SNAPSHOT)
  - [~] 4.6 Push branch, merge, and tag to remote
