# Step 1: Use Maven image to build the artifact
FROM maven:3.8.4-jdk-11 as build

# Set the working directory in the Docker image
WORKDIR /app

# Copy pom.xml and source code
COPY pom.xml .
COPY src /app/src

# Compile and package the application to an executable JAR
RUN mvn clean package -DskipTests

# Step 2: Use OpenJDK image to run the application
FROM openjdk:11-jre-slim

# Set the deployment directory
WORKDIR /app

# Copy the JAR file from the build stage
COPY --from=build /app/target/*.jar /app/app.jar

# Command to run the application
ENTRYPOINT ["java", "-jar", "app.jar"]

# Expose the port the app runs on
EXPOSE 8081
