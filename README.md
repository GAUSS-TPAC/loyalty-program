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
   Fill in the `KERNEL_CORE_URL` / `KERNEL_SERVICE_CLIENT_ID` / `KERNEL_SERVICE_CLIENT_SECRET` values if you need
   the app to talk to Kernel Core (tenant resolution, admin login, actor profiles) — see
   [Kernel Core integration](#kernel-core-integration) below.

2. **Start Infrastructure**
   Spin up the required infrastructure (Postgres, Redis, Kafka, and Keycloak) using Docker Compose:
   ```bash
   docker compose up -d
   ```
   Note the space, not a dash — that's the Docker Compose v2 plugin bundled with modern Docker installs.
   If you only have the standalone v1 binary, use `docker-compose up -d` instead.

   > **Port conflicts**: this stack binds `5432` (Postgres), `6379` (Redis), `9092`/`29092` (Kafka) and `8080`
   > (Keycloak) on your host. If another project already uses those ports, either stop it first or remap the
   > ports on the left side of `ports:` in `docker-compose.yml` (e.g. `"5433:5432"`) and update `.env` to match.

3. **Build & Run Application**
   Run the Spring Boot application using the Maven wrapper:
   ```bash
   ./mvnw spring-boot:run
   ```
   The API is available at `http://localhost:8081`.

   For local development, use the `dev` profile instead — it enables verbose logging, relaxes security,
   and injects a default tenant so you don't need a full Keycloak realm set up:
   ```bash
   ./mvnw spring-boot:run -Dspring-boot.run.profiles=dev
   ```
   The API is then available at `http://localhost:8082`.

4. **Testing**
   Run tests using Maven:
   ```bash
   ./mvnw clean verify
   ```

## Frontend

The admin portal lives in `loyalty-program-frontend/` (Next.js). With the backend running:
```bash
cd loyalty-program-frontend
npm install
npm run dev
```
Open `http://localhost:3000` — the home page is the admin login screen. Logging in with a valid
Kernel Core account redirects to `/portal` (dashboard, rules, members, wallet, events, etc.).
API calls are proxied to the backend via `NEXT_PUBLIC_API_URL` in `loyalty-program-frontend/.env.local`.

## Kernel Core integration

The backend delegates admin authentication and tenant/organization resolution to the external Kernel Core
service (`KERNEL_CORE_URL`). Every request to Kernel Core requires the `X-Client-Id` / `X-Api-Key` headers,
sourced from `KERNEL_SERVICE_CLIENT_ID` / `KERNEL_SERVICE_CLIENT_SECRET` — ask the Kernel Core team for
real values, the ones in `.env.example` are placeholders and will not authenticate. Leave
`KERNEL_TOKEN_ENDPOINT` empty; Kernel Core does not support the OAuth2 `client_credentials` grant, so the
app authenticates with the header credentials alone.

## Architecture

The project follows a Hexagonal Architecture (Ports and Adapters) pattern, using Spring WebFlux for reactive endpoints.
