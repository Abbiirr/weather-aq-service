# Weather AQ Service Guide

## Purpose
This guide serves as a comprehensive map to the project's hexagonal architecture (ports and adapters) implementation with Domain-Driven Design (DDD) principles. It explains the layer structure, boundaries, dependencies, and provides practical guidance for maintaining architectural integrity when adding new features.

## Hexagonal Architecture Implementation

### Core Principles
- **Domain at the center**: Business logic is isolated from technical concerns
- **Ports define boundaries**: Interfaces express what the system can do (inbound) and what it needs (outbound)
- **Adapters implement ports**: Technical implementations are pluggable and replaceable
- **Dependency inversion**: Outer layers depend on inner layers, never the reverse

### Layer Structure
```
src/main/java/com/dhakarun/
├── domain/          # Core business logic (framework-free)
│   ├── airquality/  # Air quality bounded context
│   ├── location/    # Location bounded context
│   ├── running/     # Running conditions bounded context
│   └── weather/     # Weather bounded context
├── application/     # Use cases and orchestration
│   ├── port/
│   │   ├── in/     # Inbound ports (use cases)
│   │   └── out/    # Outbound ports (driven ports)
│   ├── service/     # Application services implementing use cases
│   ├── bootstrap/   # System initialization logic
│   └── shared/      # Shared application concepts
├── adapter/         # Technical implementations
│   ├── in/         # Driving adapters (controllers, schedulers)
│   │   ├── web/    # REST API
│   │   ├── scheduler/ # Scheduled tasks
│   │   └── startup/ # Startup runners
│   └── out/        # Driven adapters
│       ├── persistence/ # Database access
│       ├── datasource/  # External APIs (OpenAQ, OpenMeteo)
│       └── cache/      # Redis caching
└── config/          # Spring configuration and wiring

src/main/resources/
├── application*.yml # Spring configuration profiles
└── db/migration/    # Flyway database migrations
```

### Dependency Rules
- **Domain layer**: No dependencies on other layers, completely framework-agnostic
- **Application layer**: Depends only on domain layer
- **Adapter layer**: Depends on application and domain layers
- **Configuration layer**: Can access all layers for wiring purposes only

The flow is: Adapters → Application Ports → Application Services → Domain Services → Domain Models

## Layer Responsibilities

### Domain Layer (`domain/`)
**Purpose**: Encapsulates core business logic and rules, completely independent of technical infrastructure.

**Key Components**:
- **Value Objects**: Immutable objects representing domain concepts (`LocationId`, `AQI`, `Temperature`)
- **Entities/Aggregates**: Business objects with identity (`Location`, `WeatherReading`, `AirQualityReading`)
- **Domain Services**: Pure business logic (`RunConditionEvaluator`, `AQICalculator`)
- **Repository Interfaces**: Contracts for persistence (`LocationRepository`, `WeatherRepository`)

**Rules**:
- NO framework dependencies (no Spring annotations except for repository interfaces)
- All validation in constructors - fail fast principle
- Immutable objects preferred
- Rich domain models with business methods, not anemic data structures
- Repository interfaces define what the domain needs, not how it's implemented

### Application Layer (`application/`)
**Purpose**: Orchestrates use cases by coordinating between domain logic and infrastructure concerns.

**Key Components**:
- **Inbound Ports** (`port/in/`): Use case interfaces defining what the application can do
  - Example: `GetLocationSummaryUseCase`, `BrowseLocationsUseCase`
  - Often include request/response DTOs specific to the use case
- **Outbound Ports** (`port/out/`): Interfaces for infrastructure dependencies
  - Example: `AirQualityDataSource`, `WeatherDataSource`, `CacheStore`
  - Define what the application needs from external systems
- **Application Services** (`service/`): Use case implementations
  - Example: `LocationQueryService`, `DataIngestionService`, `RunConditionService`
  - Implement inbound ports, orchestrate domain logic, call outbound ports
  - Handle transactions and cross-cutting concerns

**Rules**:
- Services should be thin orchestrators, not contain business logic
- Transaction boundaries belong here
- Can use Spring annotations for DI and transactions
- Return domain objects or use-case-specific views
- Never expose infrastructure details through ports

### Adapter Layer (`adapter/`)
**Purpose**: Implements the technical integration with external systems and frameworks.

#### Inbound Adapters (`adapter/in/`)
**Driving adapters that trigger use cases:**
- **Web** (`web/`): REST API implementation
  - Controllers expose endpoints and delegate to use case ports
  - Request/Response DTOs for API contracts
  - Mappers to convert between API DTOs and domain objects
- **Schedulers** (`scheduler/`): Time-based triggers
  - `WeatherIngestionJob`, `AirQualityIngestionJob`
- **Startup** (`startup/`): Boot-time initialization
  - `StartupIngestionRunner` for initial data loading

#### Outbound Adapters (`adapter/out/`)
**Driven adapters implementing infrastructure ports:**
- **Persistence** (`persistence/`): Database access
  - JPA entities and Spring Data repositories
  - Adapters implementing domain repository interfaces
  - Entity-to-domain mappers
- **Data Sources** (`datasource/`): External API integrations
  - `openaq/`: OpenAQ API client for air quality data
  - `openmeteo/`: OpenMeteo API client for weather data
  - DTOs for external API responses
- **Cache** (`cache/`): Redis caching implementation

**Rules**:
- All framework-specific code lives here
- Adapters translate between external formats and domain objects
- Never let infrastructure details leak into domain or application layers
- Adapters can be replaced without affecting business logic

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

## API Usage Examples

### REST API Endpoints

The service exposes the following REST endpoints through the `LocationController`:

#### 1. Get Location Summary
```bash
# Get summary for a specific location
curl -X GET "http://localhost:8080/api/locations/openaq-123/summary"
```

#### 2. Get Location Details
```bash
# Get detailed information including weather and air quality
curl -X GET "http://localhost:8080/api/locations/openaq-123"
```

#### 3. Browse Locations with Pagination
```bash
# Get first page of locations (default size: 20)
curl -X GET "http://localhost:8080/api/locations"

# Get specific page with custom size
curl -X GET "http://localhost:8080/api/locations?page=0&size=10"

# Get second page
curl -X GET "http://localhost:8080/api/locations?page=1&size=10"
```

### External API Integration Notes

#### OpenAQ API v3 Changes
The OpenAQ API v3 (as of January 2025) no longer supports direct city parameter filtering. The adapter has been updated to handle this:

**City Filtering Strategy:**
1. For known cities (Dhaka, Delhi, Mumbai, etc.), the adapter uses country-specific filtering
2. Results are then filtered client-side by city name
3. Falls back to location name matching if city field is empty

**Example OpenAQ API calls (requires API key):**
```bash
# Get locations for Bangladesh (country_id: 128)
curl -H "X-API-Key: YOUR_API_KEY" \
  "https://api.openaq.org/v3/locations?countries_id=128&limit=100"

# Get latest measurements for a location
curl -H "X-API-Key: YOUR_API_KEY" \
  "https://api.openaq.org/v3/locations/123/latest"

# Get location details
curl -H "X-API-Key: YOUR_API_KEY" \
  "https://api.openaq.org/v3/locations/123"
```

#### OpenMeteo API
```bash
# Get weather data for coordinates
curl "https://api.open-meteo.com/v1/forecast?latitude=23.8103&longitude=90.4125&current_weather=true"
```

### Database Queries

When debugging, you can query the H2 database directly:
```sql
-- Find all locations for a city
SELECT * FROM locations WHERE LOWER(city) LIKE '%dhaka%';

-- Get latest air quality readings
SELECT * FROM air_quality_readings
WHERE location_id = 'openaq-123'
ORDER BY measured_at DESC
LIMIT 1;

-- Get latest weather readings
SELECT * FROM weather_readings
WHERE location_id = 'openaq-123'
ORDER BY measured_at DESC
LIMIT 1;
```

## Quality Guardrails
- Run `./gradlew compileJava test` after refactoring or adding adapters to catch wiring issues early.
- Consider ArchUnit tests to enforce that adapters do not depend on other adapters and that domain classes remain framework-free.
- When flattening packages, move files in small batches and adjust imports immediately to keep the tree coherent.
- Test external API integrations with proper error handling for rate limits and network issues.

## Reference Order
1. `README.md` for setup and operational context.
2. `architecture.md` for detailed explanations of layers, workflows, and technology choices.
3. `GUIDE.md` (this document) for practical instructions on how to work inside the structure.
4. Browse `domain/`, then `application/`, and finally `adapter/` to follow data flow end-to-end.
