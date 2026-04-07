# AGENTS.md - AI Developer Guide for Finax Backend

## Project Overview

**Finax** is a Spring Boot 4 financial management system written in Java 25, providing revenue, expense, and investment tracking. It uses PostgreSQL with Flyway migrations, JWT-based security, and AWS integration for file storage and email delivery.

## Architecture Overview

### Layered Architecture
```
Controllers (11 total) → Services → Repository (JPA) → Database
                     ↓
              Security/Auth Layer
              Email/External Services
              Scheduled Tasks
              Event Publishing
```

### Key Architectural Decisions

1. **Stateless JWT Auth with Cookies**: Uses JWT tokens stored in HttpOnly cookies (`SecurityFilter` + `JwtCookieService`), supporting both local authentication and OAuth (Google)
2. **Service Layer Pattern**: All business logic in `services/` (16 services), using `@RequiredArgsConstructor` for DI
3. **Query DTOs with SQL Interfaces**: `dto/InterfacesSQL.java` defines projection interfaces for JPQL queries (`HomeRevenueExpense`, `MonthlyRelease`, etc.) to avoid N+1 queries
4. **Event-Driven User Onboarding**: `UserCreatedEvent` triggers async listeners for tasks like activation email scheduling
5. **Lazy Loading Strategy**: Services use `@Lazy` injection to manage circular dependencies (e.g., `ReleaseService`)
6. **Centralized Exception Handling**: `ExceptionHandlers.java` maps custom exceptions to HTTP responses via `ErrorCategory` enum

## Core Components

### Authentication & Security (`br.finax.security/`)
- **TokenService**: Generates JWT tokens (24hr expiry, São Paulo timezone)
- **SecurityFilter**: Validates JWT from cookies on each request
- **GoogleTokenVerifierService**: Validates Google OAuth tokens
- **AuthService**: Handles registration, login, OAuth flow; publishes `UserCreatedEvent`

**Key Pattern**: Use `SecurityContextHolder.getContext().getAuthentication().getPrincipal()` to get current `User` (see `UtilsService.getAuthUser()`)

### Domain Models (`br.finax.models/`)
- **User**: Implements `UserDetails`; supports AuthProvider (LOCAL, GOOGLE)
- **Release**: Financial transaction with amount, type (EXPENSE/REVENUE), date, optional repeat pattern
- **Account**: Cash account or account-like container
- **CreditCard**: Payment method with invoices and installments
- **Category**: Transaction categorization with colors
- **Invoice**: Credit card statements with payment tracking

**Pattern**: All entities use `@Data` + `@Entity` with Lombok, `userId` for multi-tenant isolation

### Service Layer (`br.finax.services/`)
**16 services follow a consistent pattern**:
1. Always check user permission (via `SecurityContextHolder`)
2. Return domain objects or DTOs (use `record` for immutable DTOs)
3. Throw `ServiceException(ErrorCategory, message)` for errors
4. Use `@Transactional` for read-only queries (`readOnly=true`)

**Example**: `ReleaseService.findById()` → check permission → throw `NotFoundException`

**Cross-Service Communication**: Services inject other services via `@RequiredArgsConstructor`

### API Controllers (`br.finax.controllers/`)
**11 controllers map to domain entities**:
- `/auth/*`: Authentication, Google OAuth, registration
- `/release/*`: CRUD + monthly view + duplicates
- `/account/*`, `/credit-card/*`, `/category/*`: Resource management
- `/reports/*`: Analytics
- `/home/*`: Dashboard data

**Pattern**: Return `ResponseEntity<T>` with domain objects; `@Valid` on DTOs; exception handling via `ExceptionHandlers`

### Data Access (`br.finax.repository/`)
- Extends `JpaRepository<Entity, ID>`
- Uses `@Query` with JPQL instead of derived methods for complex queries
- Returns **projection interfaces** from `InterfacesSQL.java` to minimize data transfer
- Example: `HomeRevenueExpense` interface with calculated fields (no JOIN in Java)

### External Integrations
- **AwsS3Service**: File upload/download, database backups (retains 10 days)
- **SesEmailProvider**: AWS SES for email delivery
- **HunterIoService**: Email validation during registration

## Critical Developer Workflows

### Setup
```bash
# Required environment variables (see docs/development.MD)
export AWS_IAM_ACCESS_KEY=...
export AWS_IAM_SECRET_KEY=...
export FINAX_DATABASE=localhost:5432/finax
export FINAX_DATABASE_USERNAME=postgres
export FINAX_DATABASE_PASSWORD=...
export HUNTER_IO_API_KEY=...

# Build & run
./mvnw clean install
./mvnw spring-boot:run -Dspring-boot.run.profiles=dev

# Tests
./mvnw test
```

### Database Migrations
- **Flyway** manages schema (12 migrations tracked in `/db/migration/`)
- Naming: `V{YYYYMMDDhhmmss}__{description}.sql`
- Never modify existing migrations; create new ones
- `application-default.properties` sets `validate-on-migrate=true`

### Building New Features
1. **Entity + Repository**: Define JPA entity, create `JpaRepository` with `@Query` methods
2. **DTOs**: Use Java `record` for immutable transfer objects (`dto/` subdirectories organized by domain)
3. **Service**: Add business logic, catch exceptions as `ServiceException`
4. **Controller**: Add endpoint, call service, return `ResponseEntity`
5. **Tests**: Place in `src/test/java/br/finax/` with `MockUtils` helper

### Email & Events
- **Sending Email**: `EmailProvider.sendMail(EmailDTO)` with SES
- **Publishing Events**: `ApplicationEventPublisher.publishEvent(new CustomEvent(...))`
- **Listening**: Use `@EventListener` + `@Async` on `@Component`
- Example: `UserCreatedEventListener` schedules account deletion after 2h2m if email not verified

### Scheduled Tasks
- `@Scheduled` in `Schedule.java` component or as `@Component` with `@EnableScheduling`
- Example: Daily database backups to S3

## Project-Specific Patterns & Conventions

### Dependency Injection
```java
// ALWAYS use @RequiredArgsConstructor with constructor injection
@Service
@RequiredArgsConstructor
public class MyService {
    private final UserRepository userRepository;  // final keyword required
    private final EmailProvider emailProvider;
}
```

### DTOs
- Use **Java records** for immutable DTOs (not Lombok `@Data`)
- Organize by domain in `dto/` subdirs: `dto/auth/LoginDTO.java`, `dto/cash_flow/MonthlyRelease.java`
- Validation via `@Valid` + Jakarta validation annotations

### Error Handling
```java
// Define custom exceptions extending ServiceException
throw new ServiceException(ErrorCategory.BAD_REQUEST, "Invalid amount");
throw new ServiceException(ErrorCategory.NOT_FOUND, "Release not found");
// ErrorCategory enum defines HTTP status (400, 404, 500, etc.)
// ExceptionHandlers.java automatically converts to ResponseEntity<ResponseError>
```

### Permission Checking
```java
// Use UtilsService.getAuthUser() to access current user
User user = getAuthUser();
if (entity.getUserId() != user.getId()) {
    throw new WithoutPermissionException();
}
```

### Database Queries
- Avoid N+1 queries: use projection interfaces
- Prefer `@Query` with explicit JPQL over derived method names
- Whenever adding a custom `@Query` in any repository, always create/update a projection interface in `dto/InterfacesSQL.java` to map the query return
- Order results consistently (e.g., `ORDER BY date, time, id`)

### Lombok & Annotation Usage
- `@RequiredArgsConstructor` for constructor injection
- `@Data` for entities (includes getters, setters, equals, hashCode)
- `@Slf4j` for logging
- `@UtilityClass` for static helper classes
- Avoid `@Transactional` on controllers; use on services

## Integration Points & External Dependencies

### AWS Integration
- **S3**: File storage (documents, backups) with configurable folder paths (`S3FolderPath` enum)
- **SES**: Transactional email
- Credentials via `AWS_IAM_ACCESS_KEY`, `AWS_IAM_SECRET_KEY` env vars

### Google OAuth
- Uses `GoogleTokenVerifierService` to verify ID tokens
- Supports account linking via `AuthProvider` enum in User model

### Email Validation
- **Hunter.io API**: Email validation during signup (via `HunterIoService`)

## Configuration & Environments

### Profiles
- `dev`: Local development, SQL logging enabled, insecure cookies
- `prod`: Production settings, secure cookies, optimized queries
- `default`: Baseline config in `application-default.properties`

### Key Properties
```properties
spring.jpa.hibernate.ddl-auto=none         # Never auto-create schema (Flyway manages)
spring.datasource.hikari.maximum-pool-size=8
spring.flyway.validate-on-migrate=true
spring.threads.virtual.enabled=true        # Java 25 virtual threads
spring.main.lazy-initialization=true
spring.jackson.time-zone=America/Sao_Paulo # Brazil timezone
```

## Testing Approach

- **Unit Tests** in `src/test/java/br/finax/`
- **MockUtils**: Helper class for test setup
- **Spring Test Support**: `@SpringBootTest`, `@WebMvcTest`, security test annotations
- No example test files visible in current tree; follow Spring Boot testing conventions

## Common Pitfalls & Best Practices

1. **Always use `final`** on class fields and local variables (project convention)
2. **Never modify existing Flyway migrations** — create new ones
3. **Use `Optional`** instead of null checks (see `docs/development.MD`)
4. **Check user ownership** before returning entities (multi-tenant isolation)
5. **Async email**: Use `@Async` listeners to avoid blocking request
6. **Lazy service injection**: Use `@Lazy` when services have circular dependencies
7. **Timezone consistency**: All timestamps use "America/Sao_Paulo" (set in `MainApplication.main()`)
8. **BigDecimal for money**: All amounts use `BigDecimal` with 2 decimal precision
9. **Release amount persistence rule**: All `Release` entities are saved with a positive `amount` in the database, regardless of being expense, revenue, or transfer

## Quick Reference: Key Files

- `MainApplication.java` — App startup, timezone setup, async/scheduling enablement
- `ExceptionHandlers.java` — Centralized HTTP error mapping
- `SecurityConfigurations.java` — JWT filter chain, CSRF disabled, stateless sessions
- `ReleaseService.java` — Complex business logic example (475 lines)
- `AuthService.java` — Authentication patterns, event publishing
- `InterfacesSQL.java` — Projection interfaces for optimized queries
- `docs/development.MD` — Setup instructions, coding patterns

---

**Generated**: 2026-03-20  
**Stack**: Java 25 + Spring Boot 4 + PostgreSQL + Flyway + JWT + AWS  
**Multi-Tenant**: Yes (per-user data isolation via `userId` field)

