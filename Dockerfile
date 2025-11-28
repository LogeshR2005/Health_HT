# ================================
# STAGE 1 - BUILD
# ================================
FROM maven:3.9.6-eclipse-temurin-17 AS build
WORKDIR /app
COPY . .
RUN mvn clean package -DskipTests

# ================================
# STAGE 2 - RUN
# ================================
FROM openjdk:17-jdk-slim
WORKDIR /app

# Copy the built jar from target folder
COPY --from=build /app/target/*.jar app.jar

EXPOSE 8080

ENTRYPOINT ["java", "-jar", "app.jar"]
