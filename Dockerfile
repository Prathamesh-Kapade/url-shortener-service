# Use official OpenJDK image
FROM maven:3.9.6-eclipse-temurin-21 AS build

# Set working directory
WORKDIR /app

# Copy jar file
COPY pom.xml .
RUN mvn dependency:go-offline -q

FROM eclipse-temurin:21-jre-alpine
WORKDIR /app

# Copy only the JAR from the build stage
COPY --from=build /app/target/*.jar app.jar

#Railway overrides with $PORT env var
EXPOSE 8080
# Run application
ENTRYPOINT ["java", "-jar", "app.jar"]