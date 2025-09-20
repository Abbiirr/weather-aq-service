# Weather AQ Service

## Overview
Weather AQ Service is a Spring Boot backend that combines weather and air-quality signals to advise runners in Dhaka. It keeps canonical location metadata, ingests data from external providers, evaluates running conditions, and exposes REST endpoints for clients.

## Current Status & Goals
- Maintains a layered hexagonal architecture with framework-free domain code and clear inbound/outbound ports.
- Repository clutter has been removed (old logs, generic help files); the focus is now on core code and documentation.
- Near-term priorities: finish the OpenAQ/OpenMeteo HTTP clients, add Redis-backed caching, and expand automated tests.
- Longer-term guidance lives in `architecture.md` for structural detail and `GUIDE.md` for day-to-day implementation steps.

## Quick Start
### Prerequisites
- Java 25
- Gradle
- PostgreSQL
- Redis

### Run locally
1. Provision infrastructure with Docker Compose:
   ```bash
   docker-compose up -d
   ```
2. Start the application:
   ```bash
   ./gradlew bootRun
   ```

### Configuration
- `src/main/resources/application.yml` holds shared defaults (datasource, Redis, Flyway, logging, scheduler cron values, provider settings).
- `application-dev.yml` and `application-prod.yml` override profile-specific settings.
- `.env.example` lists environment variables expected by Docker Compose and local runs.

## Project Layout Highlights
```
src/main/java/com/dhakarun/
|-- domain/          # Immutable business models and services
|-- application/     # Use cases, inbound/outbound ports, orchestration services
|-- adapter/
|   |-- in/          # REST controllers, schedulers, startup hooks
|   `-- out/         # Persistence, external API clients, cache adapters
`-- config/          # Spring configuration and property binding

src/main/resources/
|-- application*.yml # Profile configuration
`-- db/migration/    # Flyway SQL migrations
```
Supporting tooling lives alongside the codebase: `docker-compose.yml`, `Dockerfile`, Gradle wrapper, and helper scripts such as `update_air_quality_entity.py`.

## Data Model
Flyway migrations create three core tables:
1. `locations` - canonical metadata for tracked locations.
2. `air_quality` - pollutant measurements mapped to each location.
3. `weather` - temperature, humidity, and wind readings per location.

## Quality & Maintenance
- Keep the domain layer free of Spring dependencies and push framework concerns into adapters.
- Apply the refactoring guidance captured in `GUIDE.md` to flatten packages where it improves clarity while preserving hexagonal boundaries.
- Run `./gradlew compileJava test` after structural changes to catch import or wiring issues early.
- When adding features, update the relevant sections of this README, `architecture.md`, and `GUIDE.md` so documentation stays in sync with the code.

## Documentation
- `README.md` - high-level context, setup instructions, and maintenance reminders (this file).
- `architecture.md` - deep technical description of layers, packages, workflows, and operational concerns.
- `GUIDE.md` - onboarding, implementation playbooks, refactoring steps, and testing guidance.

