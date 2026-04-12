# ---- Build stage ----
FROM maven:3.9-eclipse-temurin-21 AS build

WORKDIR /workspace

# Copy parent POM and common module first for layer caching
COPY pom.xml ./
COPY common/pom.xml common/
COPY common/src common/src/
COPY notification-service/pom.xml notification-service/

# Download dependencies
RUN mvn -pl common,notification-service dependency:go-offline -q

# Copy notification-service source and build
COPY notification-service/src notification-service/src/
RUN mvn -pl common,notification-service -am package -DskipTests -q

# ---- Runtime stage ----
FROM eclipse-temurin:21-jre-alpine AS runtime

# Create non-root user
RUN addgroup -S glpi && adduser -S glpi -G glpi

WORKDIR /app

# Copy the built JAR
COPY --from=build /workspace/notification-service/target/notification-service-*.jar app.jar

# Set ownership
RUN chown glpi:glpi app.jar

USER glpi

EXPOSE 8087

ENTRYPOINT ["java", "-jar", "app.jar"]
