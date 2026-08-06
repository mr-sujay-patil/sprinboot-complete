# Stage 1: Build stage
# WHY: Multi-stage build compiles Java source using Maven + JDK in an isolated stage, 
# preventing build tools, Maven caches, and source code from leaking into the final runtime image.
FROM maven:3.9.6-eclipse-temurin-21-alpine AS builder
WORKDIR /app
COPY pom.xml .
# WHY: Downloading dependencies first creates a reusable Docker layer; source edits won't re-trigger full dependency downloads.
RUN mvn dependency:go-offline -B
COPY src ./src
RUN mvn package -DskipTests -B

# Stage 2: Runtime stage
# WHY: Minimal JRE Alpine base image minimizes container size (~200MB vs ~600MB) and reduces security vulnerability surface area.
FROM eclipse-temurin:21-jre-alpine
WORKDIR /app
# WHY: Copy only the final compiled executable JAR from the builder stage.
COPY --from=builder /app/target/*.jar app.jar

# WHY: Informs developers and container runtimes that Eureka Server listens on port 8761.
EXPOSE 8761

# WHY: Container memory limits (e.g. mem_limit) are enforced by cgroups; MaxRAMPercentage ensures the JVM heap 
# respects the container's assigned RAM boundaries to avoid sudden OOM (Out Of Memory) container kills.
ENTRYPOINT ["java", "-XX:MaxRAMPercentage=75.0", "-jar", "app.jar"]
