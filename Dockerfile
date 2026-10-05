# --- Build stage -------------------------------------------------------
FROM eclipse-temurin:21-jdk AS build
WORKDIR /app

# Download dependencies first so they are cached between builds
COPY .mvn/ .mvn/
COPY mvnw pom.xml ./
RUN chmod +x mvnw && ./mvnw -q -B dependency:go-offline

COPY src/ src/
RUN ./mvnw -q -B package -DskipTests

# --- Runtime stage -----------------------------------------------------
FROM eclipse-temurin:21-jre
WORKDIR /app
RUN useradd --system --uid 1001 safediary
COPY --from=build /app/target/*.jar app.jar
USER safediary

# Render injects PORT; the app reads it through server.port=${PORT:8080}
EXPOSE 8080
ENV JAVA_TOOL_OPTIONS="-XX:MaxRAMPercentage=75 -XX:+UseSerialGC"
ENTRYPOINT ["java", "-jar", "app.jar"]
