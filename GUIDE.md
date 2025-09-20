# Weather AQ Service Guide

## Purpose
Use this guide as a map to the project structure and as a playbook for working within the hexagonal layout. It explains what lives in each directory, how the layers collaborate, and how to add features while preserving the architecture.

## Layered Layout at a Glance
```
src/main/java/com/dhakarun/
|-- domain/          # Core business logic (framework-free)
|-- application/     # Use cases and orchestration services
|-- adapter/         # Inbound and outbound implementations of ports
`-- config/          # Spring wiring and property binding

src/main/resources/
|-- application*.yml # Spring configuration per profile
`-- db/migration/    # Flyway SQL migrations
```
Each layer depends only on the ones listed above it: adapters -> application -> domain. Configuration is the only layer that can see everything.

## Domain Layer (`domain/`)
- Encapsulates business language: value objects, aggregates, and pure services such as `RunConditionEvaluator`.
- Keep it free of Spring or infrastructure imports; prefer immutable types and constructor validation.
- Repository interfaces (ports) sit alongside the models they expose (for example, `LocationRepository` in `domain/location`).
- When introducing new concepts, encode invariants in constructors and expose intent-revealing methods instead of mutable setters.

## Application Layer (`application/`)
- `port/in`: inbound ports describe what callers can ask the system to do (`GetLocationSummaryUseCase`, `IngestAirQualityCommand`).
- `port/out`: outbound ports capture dependencies on infrastructure (`WeatherRepository`, `AirQualityDataSource`, `CacheStore`).
- `service`: orchestrates domain logic by implementing inbound ports and collaborating with outbound ports. Services stay thin and delegate calculations to domain objects.
- Keep transaction boundaries here (annotate service methods if needed) and return domain-centric results for adapters to shape.

## Adapter Layer (`adapter/`)
### Inbound (`adapter/in`)
- `web`: REST controllers, request/response DTOs, and mappers. Controllers depend on inbound ports, not concrete service classes.
- `scheduler` and `startup`: components that trigger application services on schedules or during boot.

### Outbound (`adapter/out`)
- `persistence`: JPA entities, Spring Data repositories, and adapters that translate between entities and domain aggregates.
- `datasource`: external API clients (OpenAQ, OpenMeteo) plus DTOs that map provider payloads into domain objects.
- `cache`: Redis-backed implementation of `CacheStore`.
Adapters own all framework-specific code and convert to or from domain types at the boundary.

## Configuration Layer (`config/`)
- Houses Spring `@Configuration` classes, property binders (`OpenAQProperties`, `OpenMeteoProperties`), and shared beans such as the application `Clock`.
- Avoid business logic; limit this layer to wiring concerns and property mapping.

## Working Within the Structure
### Adding a Domain Capability
1. Create new domain objects inside the appropriate bounded context under `domain/`.
2. Declare repository interfaces or domain services alongside those types.
3. Update application services to use the new types and rely on ports for persistence or integrations.
4. Extend adapters to map the added fields, keeping conversion logic at the boundary.

### Adding an Inbound Flow (REST endpoint, scheduler, startup task)
1. Shape the use case in `application/port/in` (new interface or command record).
2. Implement or extend an application service in `application/service`.
3. Add controller or scheduler code in `adapter/in`, translating request DTOs to domain structures.
4. Map responses back to DTOs so external payloads stay outside the domain layer.

### Adding an Outbound Integration (database change, external API, cache)
1. Define or adjust the outbound port in `application/port/out`.
2. Implement the adapter under `adapter/out` (persistence, datasource, or cache) and map to domain objects.
3. Wire the adapter via constructor injection or configuration.
4. Update services to depend on the new or expanded port operations.

## Conventions and Tips
- Keep package names meaningful: bounded contexts sit under `domain/*`, technology-specific folders under `adapter/out/*`.
- DTOs stay in adapter packages; domain objects should never expose HTTP or persistence concerns.
- Prefer value objects over primitives in domain and application layers to capture intent.
- Update both `GUIDE.md` and `architecture.md` whenever you reshape packages so documentation mirrors the codebase.

## Quality Guardrails
- Run `./gradlew compileJava test` after refactoring or adding adapters to catch wiring issues early.
- Consider ArchUnit tests to enforce that adapters do not depend on other adapters and that domain classes remain framework-free.
- When flattening packages, move files in small batches and adjust imports immediately to keep the tree coherent.

## Reference Order
1. `README.md` for setup and operational context.
2. `architecture.md` for detailed explanations of layers, workflows, and technology choices.
3. `GUIDE.md` (this document) for practical instructions on how to work inside the structure.
4. Browse `domain/`, then `application/`, and finally `adapter/` to follow data flow end-to-end.
