# ---- Build stage ----
FROM maven:3.9-eclipse-temurin-21 AS build

WORKDIR /workspace

# Copy parent POM and all module POMs for Maven reactor validation
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

# Download dependencies
RUN mvn -pl common,ticket-service dependency:go-offline -q

# Copy ticket-service source and build
COPY ticket-service/src ticket-service/src/
RUN mvn -pl common,ticket-service -am package -DskipTests -q

# ---- Runtime stage ----
FROM eclipse-temurin:21-jre-alpine AS runtime

# Create non-root user
RUN addgroup -S glpi && adduser -S glpi -G glpi

WORKDIR /app

# Copy the built JAR
COPY --from=build /workspace/ticket-service/target/ticket-service-*.jar app.jar

# Set ownership
RUN chown glpi:glpi app.jar

USER glpi

EXPOSE 8082

ENTRYPOINT ["java", "-jar", "app.jar"]
