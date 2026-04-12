# ---- Build stage ----
FROM maven:3.9-eclipse-temurin-21 AS build

WORKDIR /workspace

# Copy parent POM and common module first for layer caching
COPY pom.xml ./
COPY common/pom.xml common/
COPY common/src common/src/
COPY api-gateway/pom.xml api-gateway/

# Download dependencies
RUN mvn -pl common,api-gateway dependency:go-offline -q

# Copy api-gateway source and build
COPY api-gateway/src api-gateway/src/
RUN mvn -pl common,api-gateway -am package -DskipTests -q

# ---- Runtime stage ----
FROM eclipse-temurin:21-jre-alpine AS runtime

# Create non-root user
RUN addgroup -S glpi && adduser -S glpi -G glpi

WORKDIR /app

# Copy the built JAR
COPY --from=build /workspace/api-gateway/target/api-gateway-*.jar app.jar

# Set ownership
RUN chown glpi:glpi app.jar

USER glpi

EXPOSE 8080

ENTRYPOINT ["java", "-jar", "app.jar"]
