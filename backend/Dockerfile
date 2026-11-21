
# =====================================
# Stage 1: Build Spring Boot Application
# =====================================
FROM maven:3.9-eclipse-temurin-21 AS builder

WORKDIR /app

# Copy Maven dependencies configuration
COPY pom.xml .

# Download dependencies
RUN mvn dependency:go-offline

# Copy source code
COPY src ./src

# Build application
RUN mvn clean package -DskipTests


# =====================================
# Stage 2: Run Application
# =====================================
FROM eclipse-temurin:21-jre

WORKDIR /app

# Copy executable JAR
COPY --from=builder /app/target/backend-0.0.1-SNAPSHOT.jar app.jar

# Spring Boot port
EXPOSE 8080

# Start application
ENTRYPOINT ["java", "-jar", "app.jar"]
