# Multi-stage Dockerfile for Takharrujy Backend
# Uses Java 24 with virtual threads for optimal performance

# Build stage
FROM openjdk:24-jdk-slim AS build

# Install Maven
RUN apt-get update && apt-get install -y maven

# Set working directory
WORKDIR /app

# Copy pom.xml and download dependencies
COPY pom.xml .
RUN mvn dependency:go-offline -B

# Copy source code and build application
COPY src ./src
RUN mvn clean package -DskipTests -B

# Runtime stage
FROM openjdk:24-jre-slim AS runtime

# Create non-root user for security
RUN groupadd -r takharrujy && useradd -r -g takharrujy takharrujy

# Set working directory
WORKDIR /app

# Copy JAR from build stage
COPY --from=build /app/target/*.jar app.jar

# Create logs directory
RUN mkdir -p /app/logs && chown -R takharrujy:takharrujy /app

# Switch to non-root user
USER takharrujy

# Expose port
EXPOSE 8080

# Health check
HEALTHCHECK --interval=30s --timeout=3s --start-period=5s --retries=3 \
    CMD curl -f http://localhost:8080/actuator/health || exit 1

# Environment variables for JVM optimization
ENV JAVA_OPTS="-Xmx512m -Xms256m -XX:+UseG1GC -XX:+UseStringDeduplication"
ENV SPRING_PROFILES_ACTIVE=prod

# Run the application with virtual threads enabled
ENTRYPOINT ["sh", "-c", "java $JAVA_OPTS -Dspring.threads.virtual.enabled=true -jar app.jar"]