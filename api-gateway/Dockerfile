# Stage 1: Build stage
# WHY: Multi-stage pattern separates build environment from execution environment.
FROM maven:3.9.6-eclipse-temurin-21-alpine AS builder
WORKDIR /app
COPY pom.xml .
RUN mvn dependency:go-offline -B
COPY src ./src
RUN mvn package -DskipTests -B

# Stage 2: Runtime stage
# WHY: Alpine-based JRE image yields a compact runtime footprint.
FROM eclipse-temurin:21-jre-alpine
WORKDIR /app
COPY --from=builder /app/target/*.jar app.jar

# WHY: Exposes API Gateway on port 8080.
EXPOSE 8080

# WHY: Restricts JVM heap utilization within container cgroup boundaries.
ENTRYPOINT ["java", "-XX:MaxRAMPercentage=75.0", "-jar", "app.jar"]
