# Weather AQ Service Architecture

## Overview
Weather AQ Service aggregates weather and air-quality signals to advise runners in Dhaka. The backend stores canonical location metadata, ingests external readings, evaluates running conditions, and exposes REST APIs. The design keeps core business rules isolated from framework concerns so external interfaces can evolve independently.

## Build & Runtime Stack
- Java 25 with preview features enabled through the Gradle toolchain (`build.gradle`) and `JAVA_TOOL_OPTIONS` in `docker-compose.yml`.
- Spring Boot 3.5.0 with starters for Web, WebFlux (planned non-blocking clients), Data JPA, Data Redis, Validation, Actuator, and Scheduling.
- PostgreSQL managed via Flyway migrations (`src/main/resources/db/migration`).
- Redis via Spring Data Redis (Lettuce) exposed to the application through the `CacheStore` port.
- MapStruct and Lombok available for mapping and boilerplate reduction (current code prefers explicit mappers and immutable records).
- Test stack: JUnit 5, Rest Assured, ArchUnit, and Testcontainers (ready for future integration testing).

## Architectural Style
The codebase follows a ports-and-adapters (hexagonal) architecture:
- **Domain** holds invariants and policies with no framework dependencies.
- **Application** exposes use cases via inbound ports and coordinates domain logic while depending on outbound ports.
- **Adapters** translate between the outside world (HTTP, schedulers, databases, remote APIs, cache) and the application ports.
- **Configuration** is the only layer aware of the entire object graph and external configuration sources.

Layer boundaries should continue to point inwards (adapters -> application -> domain) so the business core remains stable as delivery mechanisms change.

## Package Reference
```
src/main/java/com/dhakarun/
|-- domain/
|   |-- airquality      # AQI, pollutant readings, repository port
|   |-- weather         # Weather readings, temperature/humidity/wind types
|   |-- location        # Location aggregate, identifiers, coordinates
|   `-- running         # Run verdict, evaluator, health-risk guidance
|-- application/
|   |-- port/in         # Use-case interfaces and command/value records
|   |-- port/out        # Abstractions for persistence, remote APIs, cache
|   `-- service         # LocationQueryService, DataIngestionService, RunConditionService
|-- adapter/
|   |-- in/web          # REST controllers and DTO mappers
|   |-- in/scheduler    # Cron-triggered ingestion jobs
|   `-- out/
|       |-- persistence  # JPA entities, Spring Data repositories, adapters
|       |-- datasource   # OpenAQ/OpenMeteo clients and DTOs
|       `-- cache        # Redis adapter that implements CacheStore
`-- config/              # ApplicationConfig, DatabaseConfig, SchedulerConfig, WebConfig, properties classes
```
The refactoring guidance in `GUIDE.md` encourages flattening overly nested packages while preserving this layered layout.

## Domain Layer
Each bounded context (airquality, weather, location, running) provides immutable value objects, aggregates, and repository abstractions. Invariants such as humidity bounds or AQI calculations live alongside the types they protect. `RunConditionEvaluator` coordinates air-quality and weather models to produce `RunVerdict` and `HealthRisk` guidance.

## Application Layer
- **Inbound ports**: `GetLocationSummaryUseCase`, `GetLocationDetailsUseCase`, `IngestAirQualityCommand`, and `IngestWeatherCommand` describe what callers can ask the system to do.
- **Outbound ports**: `LocationRepository`, `AirQualityRepository`, `WeatherRepository`, `AirQualityDataSource`, `WeatherDataSource`, and `CacheStore` define the dependencies the application expects.
- **Services**: `LocationQueryService` composes repository calls with `RunConditionEvaluator`; `DataIngestionService` orchestrates manual and scheduled refresh workflows; `RunConditionService` offers a facade for run-condition evaluation from stored readings.

## Adapter Layer
- **Inbound**: `LocationController` exposes REST endpoints for summary, details, and manual refresh while mapping responses through `LocationDtoMapper`. Scheduler components trigger periodic refresh using cron expressions configured under `app.scheduler`.
- **Outbound**: Persistence adapters translate between JPA entities (`LocationEntity`, `AirQualityEntity`, `WeatherEntity`) and domain aggregates via dedicated mappers; Spring Data repositories provide data access. Remote adapters (`OpenAQAdapter`, `OpenMeteoAdapter`) will eventually call external APIs using WebClient; today they stub responses while wiring and DTO mapping are in place. `RedisAdapter` implements `CacheStore` using `StringRedisTemplate` and Jackson; `RedisConfig` prepares the Lettuce connection factory.

## Supporting Assets
- `build.gradle`, `settings.gradle`, and `gradle.properties` configure dependencies, Java toolchain, and JVM flags (including `--enable-preview`).
- `docker-compose.yml` provisions PostgreSQL and Redis for local development; the app service mounts the workspace for fast iteration.
- `Dockerfile` builds a runnable container image with preview features enabled.
- `.env.example` documents environment variables consumed by Docker Compose and Spring profiles.
- `update_air_quality_entity.py` demonstrates a scripted migration of JPA annotations.

## Persistence & Data Model
Flyway migrations create three core tables (`locations`, `air_quality`, `weather`) plus indexes. Entities mirror the schema with foreign keys tying readings to locations. Repository adapters retrieve the latest measurements via query methods such as `findTopByLocationIdOrderByMeasuredAtDesc`.

## Configuration & Environment
`application.yml` centralizes datasource settings, transaction policies, Redis connection details, Flyway configuration, actuator exposure, logging levels, scheduler cron expressions, and external provider properties. `application-dev.yml` and `application-prod.yml` refine those defaults per profile. Configuration properties such as `OpenAQProperties` and `OpenMeteoProperties` bind provider URLs, API keys, and timeouts. Docker Compose sets sane local defaults while enabling hot reload through mounted volumes.

## Runtime Workflows
1. **Location summary (`GET /locations/{id}/summary`)**: controller -> `LocationQueryService` -> repositories -> `RunConditionEvaluator` -> DTO mapper.
2. **Location details (`GET /locations/{id}`)**: same path as summary but returns the full aggregate view.
3. **Manual refresh (`POST /locations/{id}/refresh`)**: controller delegates to `DataIngestionService.refreshFromDataSources`, which queries outbound data sources and persists results.
4. **Scheduled ingestion**: cron jobs in `adapter/in/scheduler` call the same service for predefined seed locations; cron expressions live in `application.yml`.
5. **Command-based ingestion**: external pipelines can publish `IngestAirQualityCommand` or `IngestWeatherCommand`; the application service persists readings via domain constructors.

## Observability
Actuator endpoints (`health`, `info`, `metrics`, `prometheus`) are enabled for runtime insight. Logging defaults to `DEBUG` for domain packages and `INFO` for Spring Web; adjust through `application.yml` as needed. SpringDoc (`springdoc-openapi-starter-webmvc-ui`) can expose interactive API documentation once the application runs.

## Testing Strategy
- **Domain**: instantiate value objects and services directly for fast unit tests.
- **Application**: mock outbound ports to verify orchestration and transaction boundaries.
- **Adapters**: use Spring MVC slice tests or Rest Assured for controllers, and Testcontainers for persistence/integration coverage.
- **Architecture**: ArchUnit rules (placeholder in `src/test/java`) can guard against layer violations.
Run `./gradlew compileJava test` regularly, especially after refactoring package boundaries.

## Extensibility & Refactoring Guidance
- Use outbound ports to add new providers (for example alternate weather APIs) or caches; implement adapters without leaking framework code into the application layer.
- When subpackages become excessive, follow the flattening approach in `GUIDE.md` to merge `model`, `repository`, and `service` folders into the bounded-context root.
- Before large structural moves, take a checkpoint (`git commit`) and rely on the scripted command hints in `GUIDE.md` for bulk renames and import updates.
- Seed locations for schedulers are currently hard-coded; externalize them through configuration or persistence when requirements grow.
- Add new profiles by extending the YAML hierarchy or leveraging `.env` overrides described in the README.


