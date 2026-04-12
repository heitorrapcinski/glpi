# ---- Build stage ----
FROM maven:3.9-eclipse-temurin-21 AS build

WORKDIR /workspace

# Copy parent POM and common module first for layer caching
COPY pom.xml ./
COPY common/pom.xml common/
COPY common/src common/src/
COPY change-service/pom.xml change-service/

# Download dependencies
RUN mvn -pl common,change-service dependency:go-offline -q

# Copy change-service source and build
COPY change-service/src change-service/src/
RUN mvn -pl common,change-service -am package -DskipTests -q

# ---- Runtime stage ----
FROM eclipse-temurin:21-jre-alpine AS runtime

# Create non-root user
RUN addgroup -S glpi && adduser -S glpi -G glpi

WORKDIR /app

# Copy the built JAR
COPY --from=build /workspace/change-service/target/change-service-*.jar app.jar

# Set ownership
RUN chown glpi:glpi app.jar

USER glpi

EXPOSE 8084

ENTRYPOINT ["java", "-jar", "app.jar"]
