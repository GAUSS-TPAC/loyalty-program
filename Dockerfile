# Build stage
FROM eclipse-temurin:21-jdk-jammy AS builder
WORKDIR /app
COPY .mvn/ .mvn/
COPY mvnw ./
COPY pom.xml ./
RUN chmod +x mvnw && ./mvnw dependency:go-offline

COPY src ./src
RUN ./mvnw clean package -DskipTests

# Run stage
FROM eclipse-temurin:21-jre-jammy
WORKDIR /app
COPY --from=builder /app/target/*.jar app.jar

EXPOSE 8081
ENTRYPOINT ["java", "-jar", "app.jar"]
