# Use official Maven image with Java 17 to build the app
FROM maven:3.9.6-eclipse-temurin-17 AS build
WORKDIR /app
# Copy the pom.xml and source code
COPY pom.xml .
COPY src ./src
# Compile and package the application
RUN mvn clean package -DskipTests

# Use specialized Java 17 runtime for the final image to keep it lightweight
FROM eclipse-temurin:17-jre-jammy
WORKDIR /app
# Copy the built jar file from the previous stage
COPY --from=build /app/target/AI-Resume-Analyser-0.0.1-SNAPSHOT.jar app.jar
# Expose the default port
EXPOSE 8080
# Run the application
ENTRYPOINT ["java", "-Dspring.profiles.active=h2", "-jar", "app.jar"]


