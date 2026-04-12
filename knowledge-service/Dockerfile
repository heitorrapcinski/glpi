# ---- Build stage ----
FROM maven:3.9-eclipse-temurin-21 AS build

WORKDIR /workspace

# Copy parent POM and common module first for layer caching
COPY pom.xml ./
COPY common/pom.xml common/
COPY common/src common/src/
COPY knowledge-service/pom.xml knowledge-service/

# Download dependencies
RUN mvn -pl common,knowledge-service dependency:go-offline -q

# Copy knowledge-service source and build
COPY knowledge-service/src knowledge-service/src/
RUN mvn -pl common,knowledge-service -am package -DskipTests -q

# ---- Runtime stage ----
FROM eclipse-temurin:21-jre-alpine AS runtime

# Create non-root user
RUN addgroup -S glpi && adduser -S glpi -G glpi

WORKDIR /app

# Copy the built JAR
COPY --from=build /workspace/knowledge-service/target/knowledge-service-*.jar app.jar

# Set ownership
RUN chown glpi:glpi app.jar

USER glpi

EXPOSE 8088

ENTRYPOINT ["java", "-jar", "app.jar"]
