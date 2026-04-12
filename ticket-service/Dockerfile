# ---- Build stage ----
FROM maven:3.9-eclipse-temurin-21 AS build

WORKDIR /workspace

# Copy parent POM and common module first for layer caching
COPY pom.xml ./
COPY common/pom.xml common/
COPY common/src common/src/
COPY ticket-service/pom.xml ticket-service/

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
