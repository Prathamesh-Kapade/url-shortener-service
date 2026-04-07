# -------- Stage 1: Build --------
FROM maven:3.9.6-eclipse-temurin-21 AS build

WORKDIR /app

# Copy pom.xml first (for caching)
COPY pom.xml .

# Download dependencies
RUN mvn dependency:go-offline -q

# Copy source code
COPY src ./src

# Build the jar
RUN mvn clean package -DskipTests

# -------- Stage 2: Run --------
FROM eclipse-temurin:21-jre-alpine

WORKDIR /app

# Copy built jar from build stage
COPY --from=build /app/target/*.jar app.jar

# Railway uses dynamic PORT
EXPOSE 8080

# Run app
ENTRYPOINT ["java", "-jar", "app.jar"]