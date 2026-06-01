# Loyalty Program - Backend API

This is the backend for the Loyalty Program application, a SaaS multi-tenant platform built with Java 21, Spring Boot 3.5, WebFlux, and R2DBC.

## Tech Stack
- **Java 21**
- **Spring Boot 3.5** (WebFlux, Data R2DBC, Security OAuth2 Resource Server)
- **PostgreSQL** (Relational Database)
- **Redis** (Caching and Idempotency)
- **Kafka** (Event Streaming)
- **Keycloak** (OAuth2 Identity Provider)
- **Flyway** (Database Migrations)

## Prerequisites
- Java 21
- Maven (`./mvnw` provided)
- Docker & Docker Compose

## Local Development Setup

1. **Environment Variables**
   Create a `.env` file in the root directory based on the `.env.example` provided:
   ```bash
   cp .env.example .env
   ```

2. **Start Infrastructure**
   Spin up the required infrastructure (Postgres, Redis, Kafka, and Keycloak) using Docker Compose:
   ```bash
   docker-compose up -d
   ```

3. **Build & Run Application**
   Run the Spring Boot application using the Maven wrapper:
   ```bash
   ./mvnw spring-boot:run
   ```
   The API will be available at `http://localhost:8081`.

4. **Testing**
   Run tests using Maven:
   ```bash
   ./mvnw clean verify
   ```

## Architecture

The project follows a Hexagonal Architecture (Ports and Adapters) pattern, using Spring WebFlux for reactive endpoints.
