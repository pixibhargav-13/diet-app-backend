# Stage 1: Build
FROM maven:3.9.6-eclipse-temurin-21 AS builder
WORKDIR /app

# Copy root pom and all module poms first (for layer caching)
COPY pom.xml .
COPY api-contract/pom.xml api-contract/
COPY user-management/pom.xml user-management/
COPY session-management/pom.xml session-management/
COPY client-management/pom.xml client-management/
COPY consultation-management/pom.xml consultation-management/
COPY nutrition-management/pom.xml nutrition-management/
COPY progress-management/pom.xml progress-management/
COPY shop-management/pom.xml shop-management/
COPY web-app/pom.xml web-app/

# Download dependencies (cached if poms haven't changed)
RUN mvn dependency:go-offline -B

# Copy all source code
COPY . .

# Build the project, skip tests for faster deploy
RUN mvn clean package -DskipTests -B

# Stage 2: Run
FROM eclipse-temurin:21-jre-jammy
WORKDIR /app

COPY --from=builder /app/web-app/target/web-app-1.0.0-SNAPSHOT.jar app.jar

EXPOSE 8080

ENTRYPOINT ["java", "-jar", "app.jar"]
