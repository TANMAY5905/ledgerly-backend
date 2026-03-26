# ---------- BUILD STAGE ----------
FROM maven:3.9.9-eclipse-temurin-17 AS build
WORKDIR /app

# Copy everything
COPY . .

# Build jar
RUN mvn clean package -DskipTests


# ---------- RUN STAGE ----------
FROM eclipse-temurin:17-jdk-jammy
WORKDIR /app

# Copy built jar from previous stage
COPY --from=build /app/target/*.jar app.jar

# Run the app
ENTRYPOINT ["java", "-jar", "app.jar"]